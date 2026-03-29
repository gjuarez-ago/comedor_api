package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.ProductoValidadoDTO;
import com.services.comedor.models.ValidacionQRResponse;
import com.services.comedor.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.services.comedor.models.ConsumoResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaService {

    private static final Long TIPO_TIENDA_ID = 99L;

    private final ConsumoRepository consumoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ValidacionService validacionService;
    private final InventarioService inventarioService;
    private final ConsumoEstadoHistorialRepository consumoEstadoHistorialRepository;
    private final ProductoStockRepository productoStockRepository;


    

    // =====================================================
// 3. CANCELAR PEDIDO
// =====================================================

/**
 * ❌ Cancelar pedido (error humano, falta de ingredientes, etc.)
 * 
 * Este método permite cancelar un pedido en cualquier estado antes de ser entregado.
 * Es utilizado por cajeros, cocineros o jefes de comedor cuando ocurre algún problema.
 * 
 * ============================================================================
 * FLUJO DE EJECUCIÓN
 * ============================================================================
 * 
 * 1. Validaciones Previas
 *    ├── Verifica que el pedido exista
 *    ├── Verifica que no esté ya entregado
 *    └── Verifica que no esté ya cancelado
 * 
 * 2. Devolución de Stock (si aplica)
 *    └── Si es snack y estaba en estado PAGADO → devuelve stock
 *    └── Si es comida con porciones y estaba en PAGADO → devuelve stock
 * 
 * 3. Marcar como Merma (si aplica)
 *    └── Si estaba en PREPARANDO o LISTO → marca como merma (comida perdida)
 * 
 * 4. Registro de Cancelación
 *    ├── Guarda motivo de cancelación
 *    ├── Registra usuario que canceló
 *    └── Registra fecha y hora
 * 
 * 5. Cambio de Estado
 *    └── Estado actual → CANCELADO
 * 
 * ============================================================================
 * REGLAS DE NEGOCIO POR TIPO Y ESTADO
 * ============================================================================
 * 
 * | Tipo        | Estado      | Devuelve Stock | Marca Merma | ¿Por qué?                    |
 * |-------------|-------------|----------------|-------------|------------------------------|
 * | Snack       | CREADO      | ❌ No          | ❌ No       | No se descontó stock aún     |
 * | Snack       | PAGADO      | ✅ Sí          | ❌ No       | Stock descontado, se devuelve |
 * | Snack       | PREPARANDO  | ❌ No          | ❌ No       | Snack no va a cocina         |
 * | Comida      | CREADO      | ❌ No          | ❌ No       | No se descontó stock aún     |
 * | Comida      | PAGADO      | ✅ Sí          | ❌ No       | Stock descontado, se devuelve |
 * | Comida      | PREPARANDO  | ❌ No          | ✅ Sí       | Comida en preparación, se pierde |
 * | Comida      | LISTO       | ❌ No          | ✅ Sí       | Comida lista, no se entregó  |
 * | Cualquiera  | ENTREGADO   | ❌ No          | ❌ No       | No se puede cancelar         |
 * | Cualquiera  | CANCELADO   | ❌ No          | ❌ No       | Ya está cancelado            |
 * 
 * ============================================================================
 * EJEMPLOS DE USO
 * ============================================================================
 * 
 * @example Snack cancelado antes de escanear
 *   Estado: CREADO
 *   Proceso: No descuenta stock, no marca merma
 *   Resultado: CREADO → CANCELADO
 * 
 * @example Snack cancelado después de escanear (PAGADO)
 *   Estado: PAGADO
 *   Proceso: Devuelve stock al inventario
 *   Resultado: PAGADO → CANCELADO
 * 
 * @example Comida cancelada en PAGADO (antes de cocinar)
 *   Estado: PAGADO
 *   Proceso: Devuelve stock (si controla porciones), no marca merma
 *   Resultado: PAGADO → CANCELADO
 * 
 * @example Comida cancelada en PREPARANDO (cocina ya empezó)
 *   Estado: PREPARANDO
 *   Proceso: No devuelve stock, marca como merma
 *   Resultado: PREPARANDO → CANCELADO, esMerma = true
 * 
 * @example Comida cancelada en LISTO (ya preparada)
 *   Estado: LISTO
 *   Proceso: No devuelve stock, marca como merma
 *   Resultado: LISTO → CANCELADO, esMerma = true
 * 
 * ============================================================================
 * 
 * @param consumoId ID del pedido a cancelar
 * @param usuarioId ID del usuario que cancela (cajero, cocinero, admin)
 * @param motivo Razón de la cancelación (ej: "Cliente no llegó", "Falta de ingredientes")
 * @return ConsumoResponse con estado actualizado a CANCELADO
 * @throws BusinessException con códigos:
 *         - CON_004: Pedido no encontrado
 *         - CON_008: No se puede cancelar pedido entregado
 *         - CON_011: Pedido ya estaba cancelado
 */
@Transactional
public ConsumoResponse cancelarPedido(Long consumoId, Long usuarioId, String motivo) {
    
    // =====================================================
    // 1. OBTENER USUARIO
    // =====================================================
    
    Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new BusinessException(
                    "USR_001",
                    "Usuario no encontrado",
                    HttpStatus.NOT_FOUND));
    
    // =====================================================
    // 2. BUSCAR CONSUMO
    // =====================================================
    
    Consumo consumo = consumoRepository.findById(consumoId)
            .orElseThrow(() -> new BusinessException(
                    "CON_004",
                    "Pedido no encontrado",
                    HttpStatus.NOT_FOUND));
    
    // =====================================================
    // 3. VALIDAR QUE NO ESTÉ ENTREGADO
    // =====================================================
    
    if (consumo.getEstado() == EstadoConsumo.ENTREGADO) {
        throw new BusinessException(
                "CON_008",
                "No se puede cancelar un pedido que ya fue entregado",
                HttpStatus.BAD_REQUEST);
    }
    
    // =====================================================
    // 4. VALIDAR QUE NO ESTÉ YA CANCELADO
    // =====================================================
    
    if (consumo.getEstado() == EstadoConsumo.CANCELADO) {
        throw new BusinessException(
                "CON_011",
                "Este pedido ya fue cancelado anteriormente",
                HttpStatus.BAD_REQUEST);
    }
    
    // =====================================================
    // 5. DETERMINAR TIPO Y ESTADO
    // =====================================================
    
    boolean esSnack = TIPO_TIENDA_ID.equals(consumo.getTipoConsumo().getId());
    EstadoConsumo estadoActual = consumo.getEstado();
    
    // =====================================================
    // 6. DEVOLVER STOCK SI CORRESPONDE
    // =====================================================
    
    // Devolver stock si:
    // - Es snack y estaba en PAGADO (stock descontado)
    // - Es comida con porciones y estaba en PAGADO (stock descontado)
    boolean debeDevolverStock = false;
    
    if (esSnack && estadoActual == EstadoConsumo.PAGADO) {
        debeDevolverStock = true;
    }
    
    if (!esSnack && estadoActual == EstadoConsumo.PAGADO) {
        // Verificar si el producto controla porciones
        for (ConsumoDetalle detalle : consumo.getDetalles()) {
            if (detalle.getProducto().getControlaPorciones()) {
                debeDevolverStock = true;
                break;
            }
        }
    }
    
    if (debeDevolverStock) {
        for (ConsumoDetalle detalle : consumo.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getControlaInventario() || producto.getControlaPorciones()) {
                inventarioService.devolverStock(
                        detalle.getProducto().getId(),
                        consumo.getComedor().getId(),
                        detalle.getCantidad()
                );
                log.debug("Stock devuelto - Producto: {}, Cantidad: {}", 
                        producto.getNombre(), detalle.getCantidad());
            }
        }
    }
    
    // =====================================================
    // 7. MARCAR COMO MERMA SI CORRESPONDE
    // =====================================================
    
    // Marcar como merma si:
    // - Es comida y estaba en PREPARANDO o LISTO (comida ya preparada)
    boolean esMerma = false;
    
    if (!esSnack && (estadoActual == EstadoConsumo.PREPARANDO || estadoActual == EstadoConsumo.LISTO)) {
        esMerma = true;
        log.warn("Merma registrada - Consumo: {}, Productos: {}",
                consumo.getId(),
                consumo.getDetalles().stream()
                        .map(d -> d.getProducto().getNombre())
                        .collect(Collectors.joining(", ")));
    }
    
    // =====================================================
    // 8. ACTUALIZAR CONSUMO
    // =====================================================
    
    consumo.setEstado(EstadoConsumo.CANCELADO);
    consumo.setUsuarioCancela(usuario);
    consumo.setMotivoCancelacion(motivo);
    consumo.setFechaCancelacion(LocalDateTime.now());
    consumo.setEsMerma(esMerma);
    consumo = consumoRepository.save(consumo);
    
    // =====================================================
    // 9. REGISTRAR HISTORIAL
    // =====================================================
    
    registrarHistorial(consumo, EstadoConsumo.CANCELADO, usuario);
    
    // =====================================================
    // 10. LOG
    // =====================================================
    
    log.info("Pedido cancelado - Consumo: {}, Usuario: {}, Motivo: {}, EsMerma: {}",
            consumo.getId(),
            usuario.getUsername(),
            motivo,
            esMerma);
    
    // =====================================================
    // 11. RETORNAR RESPUESTA
    // =====================================================
    
    return new ConsumoResponse(
            consumo.getId(),
            consumo.getTokenQr(),
            consumo.getEstado().name()
    );
}


    // =====================================================
// 2. ENTREGAR PEDIDO (SOLO PARA COMIDAS)
// =====================================================

/**
 * 🍽️ Cajero entrega pedido al empleado (solo para comidas que están LISTO)
 * 
 * FLUJO:
 * 1. Snacks: NO se usa este método (ya se entregaron en validarQR)
 * 2. Comidas: Se ejecuta cuando cocina marca LISTO
 * 3. Cambia estado: LISTO → ENTREGADO
 * 
 * @param consumoId ID del pedido a entregar
 * @param usuarioId ID del cajero autenticado
 * @return ConsumoResponse con estado actualizado
 */
@Transactional
public ConsumoResponse entregarPedido(Long consumoId, Long usuarioId) {
    
    // 1. OBTENER CAJERO
    Usuario cajero = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new BusinessException(
                    "USR_001", 
                    "Usuario no encontrado", 
                    HttpStatus.NOT_FOUND));
    
    // 2. BUSCAR CONSUMO
    Consumo consumo = consumoRepository.findById(consumoId)
            .orElseThrow(() -> new BusinessException(
                    "CON_004", 
                    "Pedido no encontrado", 
                    HttpStatus.NOT_FOUND));
    
    // 3. VALIDAR QUE NO SEA SNACK
    boolean esSnack = TIPO_TIENDA_ID.equals(consumo.getTipoConsumo().getId());
    
    if (esSnack) {
        // Snacks ya se entregaron en validarQR, no deberían llegar aquí
        throw new BusinessException(
                "CON_009", 
                "Los snacks se entregan directamente en validación", 
                HttpStatus.BAD_REQUEST);
    }
    
    // 4. VALIDAR ESTADO (debe estar LISTO)
    if (consumo.getEstado() != EstadoConsumo.LISTO) {
        throw new BusinessException(
                "CON_007", 
                String.format("El pedido no está listo para entregar. Estado actual: %s", 
                        consumo.getEstado()),
                HttpStatus.BAD_REQUEST);
    }
    
    // 5. VALIDAR QUE NO ESTÉ YA ENTREGADO
    if (consumo.getEstado() == EstadoConsumo.ENTREGADO) {
        throw new BusinessException(
                "CON_008", 
                "Este pedido ya fue entregado", 
                HttpStatus.BAD_REQUEST);
    }
    
    // 6. CAMBIAR ESTADO A ENTREGADO
    consumo.setEstado(EstadoConsumo.ENTREGADO);
    consumo.setUsuarioValida(cajero);
    consumo = consumoRepository.save(consumo);
    
    // 7. REGISTRAR HISTORIAL
    registrarHistorial(consumo, EstadoConsumo.ENTREGADO, cajero);
    
    // 8. LOG
    log.info("Pedido entregado - Consumo: {}, Cajero: {}, Tipo: {}",
            consumo.getId(),
            cajero.getUsername(),
            consumo.getTipoConsumo().getNombre());
    
    // 9. RETORNAR RESPUESTA
    return new ConsumoResponse(
            consumo.getId(),
            consumo.getTokenQr(),
            consumo.getEstado().name()
    );
}

    /**
     * 💳 Cajero escanea el QR del empleado para validar el pedido
     *
     * Este método es el punto de entrada principal para la validación de
     * pedidos en caja. Realiza todas las validaciones necesarias y determina el
     * flujo según el tipo de producto.
     *
     * ============================================================================
     * FLUJO DE VALIDACIÓN
     * ============================================================================
     *
     * 1. Validación Rápida (sin bloqueo) └── Verifica que el QR exista, esté en
     * estado CREADO y sea del día actual └── Si falla → Error CON_002
     *
     * 2. Bloqueo Pesimista (SELECT FOR UPDATE) └── Previene que dos cajeros
     * escaneen el mismo QR simultáneamente └── Garantiza que cada QR se procese
     * una sola vez
     *
     * 3. Validaciones de Negocio ├── Vigencia del QR: Snacks 30 min, Comidas
     * hasta fin de horario + 30 min └── Doble consumo: Solo para comidas
     * (snacks no tienen límite diario)
     *
     * 4. Descuento de Stock (si aplica) ├── Snacks: Siempre descuentan stock
     * (controla_inventario = true) └── Comidas: Descuentan stock si
     * controla_porciones = true (porciones limitadas)
     *
     * 5. Cambio de Estado ├── Snack: CREADO → ENTREGADO (directo, no pasa por
     * cocina) └── Comida: CREADO → PAGADO (pasa a cola de cocina)
     *
     * 6. Auditoría └── Registra historial de cambio de estado
     *
     * ============================================================================
     * EJEMPLOS DE USO
     * ============================================================================
     *
     * @example Snack (Galleta) Input: QR token de galleta Proceso: 1.
     * Validaciones OK 2. Descuenta 1 unidad de stock 3. Estado: CREADO →
     * ENTREGADO Output: { "id": 101, "qrToken": "xyz", "estado": "ENTREGADO" }
     *
     * @example Comida con porciones limitadas (COMIDA CORRIENTE) Input: QR
     * token de comida Proceso: 1. Validaciones OK (vigencia, doble consumo) 2.
     * Descuenta 1 porción del stock 3. Estado: CREADO → PAGADO Output: { "id":
     * 100, "qrToken": "abc", "estado": "PAGADO" }
     *
     * @example QR expirado Input: QR generado hace más de 30 min (snack) o
     * fuera de horario (comida) Output: Error CON_010 - "QR expirado. Genera
     * uno nuevo"
     *
     * @example Doble consumo (misma comida en el día) Input: QR de COMIDA
     * cuando ya consumió COMIDA hoy Output: Error CON_001 - "Ya consumiste tu
     * COMIDA hoy"
     *
     * ============================================================================
     * REGLAS DE NEGOCIO
     * ============================================================================
     *
     * | Tipo | Valida Horario | Valida Doble Consumo | Descuenta Stock | Estado
     * Final |
     * |------|----------------|---------------------|-----------------|--------------|
     * | Snack | ❌ No | ❌ No | ✅ Sí | ENTREGADO | | Comida sin porciones | ✅ Sí
     * | ✅ Sí | ❌ No | PAGADO | | Comida con porciones | ✅ Sí | ✅ Sí | ✅ Sí |
     * PAGADO |
     *
     * ============================================================================
     *
     * @param token QR token escaneado por el cajero (UUID almacenado en
     * consumo.tokenQr)
     * @param usuarioId ID del cajero autenticado (obtenido del token JWT)
     * @return ValidacionQRResponse con ID del consumo, QR token y estado actualizado
     * @throws BusinessException con códigos: - USR_001: Usuario no encontrado -
     * CON_002: QR inválido o expirado - CON_003: Pedido ya procesado - CON_010:
     * QR expirado por tiempo - CON_001: Doble consumo detectado - STK_002:
     * Stock insuficiente
     *
     * @author TuNombre
     * @since 1.0
     */
    @Transactional
public ValidacionQRResponse validarQR(String token, Long usuarioId) {

    // 1. OBTENER CAJERO
    Usuario cajero = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new BusinessException(
                    "USR_001",
                    "Usuario no encontrado",
                    HttpStatus.NOT_FOUND));

    // 2. VALIDACIÓN RÁPIDA
    boolean esValido = consumoRepository.esQrValidoParaEscanear(
            token,
            cajero.getComedorBase().getId(),
            LocalDate.now()
    );

    if (!esValido) {
        throw new BusinessException(
                "CON_002",
                "QR inválido o expirado. Genera uno nuevo",
                HttpStatus.BAD_REQUEST);
    }

    // 3. BLOQUEO PESIMISTA
    Consumo consumo = consumoRepository.findByTokenQrForUpdate(token)
            .orElseThrow(() -> new BusinessException(
                    "CON_002",
                    "QR inválido",
                    HttpStatus.NOT_FOUND));

    // 4. VALIDAR ESTADO
    if (consumo.getEstado() != EstadoConsumo.CREADO) {
        throw new BusinessException(
                "CON_003",
                "Este pedido ya fue procesado",
                HttpStatus.BAD_REQUEST);
    }

    // 5. VALIDAR VIGENCIA
    validacionService.validarVigenciaQR(consumo, LocalDateTime.now());

    // 6. DETERMINAR TIPO
    boolean esSnack = TIPO_TIENDA_ID.equals(consumo.getTipoConsumo().getId());

    // 7. VALIDAR DOBLE CONSUMO (SOLO COMIDAS)
    if (!esSnack) {
        validacionService.validarDobleConsumo(
                consumo.getEmpleado().getId(),
                consumo.getTipoConsumo().getId()
        );
    }

    // =====================================================
    // 8. PROCESAR PRODUCTOS Y RECOLECTAR INFORMACIÓN
    // =====================================================
    
    List<ProductoValidadoDTO> productosValidados = new ArrayList<>();
    boolean requierePreparacion = false;
    
    for (ConsumoDetalle detalle : consumo.getDetalles()) {
        Producto producto = detalle.getProducto();
        
        // Verificar si requiere preparación (cocina)
        if (producto.getRequierePreparacion()) {
            requierePreparacion = true;
        }
        
        // Determinar si controla stock
        boolean controlaStock = producto.getControlaInventario() || producto.getControlaPorciones();
        Integer stockRestante = null;
        
        // Descontar stock si aplica
        if (controlaStock) {
            Integer stockActual = productoStockRepository.findStockActual(
                    detalle.getProducto().getId(), 
                    consumo.getComedor().getId()
            ).orElse(0);
            
            inventarioService.descontarStock(
                    detalle.getProducto().getId(),
                    consumo.getComedor().getId(),
                    detalle.getCantidad(),
                    consumo
            );
            
            stockRestante = Math.max(0, stockActual - detalle.getCantidad());
        }
        
        // Obtener modificadores
        List<String> modificadores = detalle.getModificadores().stream()
                .map(ConsumoDetalleModificador::getNombreOpcion)
                .collect(Collectors.toList());
        
        // Agregar a la lista de productos validados
        productosValidados.add(new ProductoValidadoDTO(
                producto.getId(),
                producto.getNombre(),
                detalle.getCantidad(),
                modificadores,
                controlaStock,
                stockRestante
        ));
    }
    
    // =====================================================
    // 9. CAMBIAR ESTADO SEGÚN TIPO
    // =====================================================
    
    EstadoConsumo estadoFinal;
    boolean esEntregaRapida;
    
    if (esSnack) {
        estadoFinal = EstadoConsumo.ENTREGADO;
        esEntregaRapida = true;
        log.info("Snack entregado - Consumo: {}, Cajero: {}, Productos: {}",
                consumo.getId(), cajero.getUsername(), productosValidados.size());
    } else {
        estadoFinal = EstadoConsumo.PAGADO;
        esEntregaRapida = false;
        log.info("Pedido validado - Consumo: {}, Cajero: {}, Tipo: {}, Productos: {}",
                consumo.getId(), 
                cajero.getUsername(), 
                consumo.getTipoConsumo().getNombre(),
                productosValidados.size());
    }
    
    consumo.setEstado(estadoFinal);
    consumo.setUsuarioValida(cajero);
    consumo = consumoRepository.save(consumo);

    // 10. REGISTRAR HISTORIAL
    registrarHistorial(consumo, estadoFinal, cajero);

    // 11. RETORNAR RESPUESTA DETALLADA
    return new ValidacionQRResponse(
            consumo.getId(),
            consumo.getTokenQr(),
            estadoFinal.name(),
            esEntregaRapida,
            requierePreparacion,
            productosValidados
    );
}

    
    private void registrarHistorial(Consumo consumo, EstadoConsumo estado, Usuario usuario) {
        try {
            ConsumoEstadoHistorial historial = ConsumoEstadoHistorial.builder()
                    .consumo(consumo)
                    .estado(estado)
                    .usuario(usuario)
                    .build();
            consumoEstadoHistorialRepository.save(historial);
        } catch (Exception e) {
            log.warn("No se pudo registrar historial: {}", e.getMessage());
        }
    }

}
