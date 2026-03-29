package com.services.comedor.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.comedor.entity.Comedor;
import com.services.comedor.entity.ComedorProducto;
import com.services.comedor.entity.Consumo;
import com.services.comedor.entity.ConsumoDetalle;
import com.services.comedor.entity.ConsumoDetalleModificador;
import com.services.comedor.entity.ConsumoEstadoHistorial;
import com.services.comedor.entity.Empleado;
import com.services.comedor.entity.Horario;
import com.services.comedor.entity.OpcionModificador;
import com.services.comedor.entity.Producto;
import com.services.comedor.entity.TipoConsumo;
import com.services.comedor.entity.Usuario;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.CancelacionResponse;
import com.services.comedor.models.ConsumoDirectoRequest;
import com.services.comedor.models.CrearConsumoRequest;
import com.services.comedor.models.DetalleRequest;
import com.services.comedor.models.EstadoPedidoResponse;
import com.services.comedor.models.EstadoPedidoSimple;
import com.services.comedor.models.PedidoResponse;
import com.services.comedor.repository.ComedorProductoRepository;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.ConsumoDetalleModificadorRepository;
import com.services.comedor.repository.ConsumoDetalleRepository;
import com.services.comedor.repository.ConsumoEstadoHistorialRepository;
import com.services.comedor.repository.ConsumoRepository;
import com.services.comedor.repository.EmpleadoRepository;
import com.services.comedor.repository.HorarioRepository;
import com.services.comedor.repository.OpcionModificadorRepository;
import com.services.comedor.repository.ProductoRepository;
import com.services.comedor.repository.ProductoStockRepository;
import com.services.comedor.repository.TipoConsumoRepository;
import com.services.comedor.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

// ... imports existentes ...
@Service
@RequiredArgsConstructor
public class ConsumoService {

    private static final Logger log = LoggerFactory.getLogger(ConsumoService.class);  // 🔥 LOG MANUAL
    private static final Long TIPO_TIENDA_ID = 99L;

    private final HorarioRepository horarioRepository;
    private final ConsumoRepository consumoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final ProductoStockRepository productoStockRepository;
    private final ConsumoDetalleRepository consumoDetalleRepository;
    private final FolioService folioService;
    private final ProductoRepository productoRepository;
    private final ComedorProductoRepository comedorProductoRepository;
    private final ConsumoDetalleModificadorRepository consumoDetalleModificadorRepository;
    private final ConsumoEstadoHistorialRepository consumoEstadoHistorialRepository;
    private final OpcionModificadorRepository opcionModificadorRepository;
    private final ValidacionService validacionService;
    private final InventarioService inventarioService;
    private final UsuarioRepository usuarioRepository;
    private final ComedorRepository comedorRepository;

    // =====================================================
// 4. VENTA DIRECTA (MOSTRADOR)
// =====================================================
    /**
     * 📱 Venta Directa - Para empleados sin celular (mostrador)
     *
     * Este método se utiliza cuando el empleado NO tiene celular o no puede
     * generar su propio QR. El cajero crea el pedido directamente desde la
     * tablet.
     *
     * FLUJO: 1. Cajero autenticado (token JWT contiene usuarioId) 2. Cajero
     * ingresa número de empleado y selecciona productos 3. Cajero selecciona
     * comedor donde está físicamente (auditoría) 4. Sistema valida todo y crea
     * consumo en estado PAGADO 5. Si solo snacks → ENTREGADO directo 6. Si hay
     * comida → PAGADO (va a cocina)
     *
     * @param request DTO con número de empleado, comedor y productos
     * @param usuarioId ID del cajero autenticado (del token JWT)
     * @return PedidoResponse con folio, QR token y estado final
     */
    @Transactional
    public PedidoResponse ventaDirecta(ConsumoDirectoRequest request, Long usuarioId) {

        // =====================================================
        // 1. OBTENER CAJERO
        // =====================================================
        Usuario cajero = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException(
                "USR_001",
                "Usuario no encontrado",
                HttpStatus.NOT_FOUND));

        // =====================================================
        // 2. OBTENER EMPLEADO
        // =====================================================
        Empleado empleado = empleadoRepository.findByNumeroEmpleado(request.numeroEmpleado())
                .orElseThrow(() -> new BusinessException(
                "EMP_001",
                "Empleado no encontrado",
                HttpStatus.NOT_FOUND));

        if (!empleado.getActivo()) {
            throw new BusinessException(
                    "EMP_002",
                    "Empleado inactivo",
                    HttpStatus.FORBIDDEN);
        }

        // =====================================================
        // 3. OBTENER COMEDOR (AUDITORÍA)
        // =====================================================
        Comedor comedor = comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException(
                "COM_001",
                "Comedor no encontrado",
                HttpStatus.NOT_FOUND));

        // =====================================================
        // 4. VALIDAR ACCESO DEL CAJERO AL COMEDOR
        // =====================================================
        if (!cajero.getComedoresPermitidos().contains(comedor)) {
            throw new BusinessException(
                    "USR_005",
                    "No tienes acceso a este comedor",
                    HttpStatus.FORBIDDEN);
        }

        // =====================================================
        // 5. VALIDAR PRODUCTOS Y DETERMINAR TIPO
        // =====================================================
        // Verificar si todos los productos son snacks
        boolean todosSnacks = request.detalles().stream().allMatch(detalle -> {
            Producto p = productoRepository.findById(detalle.productoId())
                    .orElseThrow(() -> new BusinessException(
                    "PROD_001",
                    String.format("Producto no encontrado: ID %d", detalle.productoId()),
                    HttpStatus.NOT_FOUND));
            return !p.getRequierePreparacion();
        });

        // Determinar tipo de consumo
        TipoConsumo tipo;
        if (todosSnacks) {
            // Solo snacks → TIENDA
            tipo = tipoConsumoRepository.findById(TIPO_TIENDA_ID)
                    .orElseThrow(() -> new BusinessException(
                    "TIPO_001",
                    "Tipo TIENDA no configurado",
                    HttpStatus.NOT_FOUND));
        } else {
            // Hay comidas → determinar por hora actual
            LocalTime ahora = LocalTime.now();
            Horario horarioActivo = horarioRepository.findActiveByTime(comedor.getId(), ahora)
                    .orElseThrow(() -> new BusinessException(
                    "HOR_002",
                    "No hay servicio activo en este momento",
                    HttpStatus.BAD_REQUEST));
            tipo = horarioActivo.getTipoConsumo();
        }

        // =====================================================
        // 6. VALIDAR PERMISOS DEL EMPLEADO
        // =====================================================
        if (!empleado.puedeConsumir(tipo)) {
            String tiposPermitidos = empleado.getConsumosPermitidos().stream()
                    .map(TipoConsumo::getNombre)
                    .collect(Collectors.joining(", "));

            throw new BusinessException(
                    "EMP_004",
                    String.format("Empleado no tiene derecho a %s. Permisos: %s",
                            tipo.getNombre(), tiposPermitidos),
                    HttpStatus.FORBIDDEN);
        }

        // =====================================================
        // 7. VALIDAR HORARIO (SOLO SI HAY COMIDAS)
        // =====================================================
        boolean tieneComida = !todosSnacks;

        if (tieneComida) {
            validacionService.validarHorario(empleado, tipo, LocalDateTime.now());
        }

        // =====================================================
        // 8. VALIDAR DOBLE CONSUMO (SOLO COMIDAS)
        // =====================================================
        if (tieneComida) {
            validacionService.validarDobleConsumo(empleado.getId(), tipo.getId());
        }

        // =====================================================
        // 9. VALIDAR STOCK
        // =====================================================
        for (DetalleRequest detalle : request.detalles()) {
            Producto producto = productoRepository.findById(detalle.productoId())
                    .orElseThrow(() -> new BusinessException(
                    "PROD_001",
                    String.format("Producto no encontrado: ID %d", detalle.productoId()),
                    HttpStatus.NOT_FOUND));

            if (producto.getControlaInventario() || producto.getControlaPorciones()) {
                inventarioService.validarStock(
                        detalle.productoId(),
                        comedor.getId(),
                        detalle.cantidad()
                );
            }
        }

        // =====================================================
        // 10. CREAR CONSUMO (DIRECTO EN PAGADO)
        // =====================================================
        String folio = folioService.nextFolioDiario(comedor.getNombre());
        String qrToken = UUID.randomUUID().toString();

        Consumo consumo = Consumo.builder()
                .empleado(empleado)
                .comedor(comedor) // ← Para auditoría: dónde se realizó la venta
                .tipoConsumo(tipo)
                .estado(EstadoConsumo.PAGADO)
                .tokenQr(qrToken)
                .fecha(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .usuarioValida(cajero)
                .motivoDirecto("VENTA_DIRECTA")
                .build();

        consumo = consumoRepository.save(consumo);

        // =====================================================
        // 11. AGREGAR DETALLES
        // =====================================================
        for (DetalleRequest detalle : request.detalles()) {
            Producto producto = productoRepository.findById(detalle.productoId())
                    .orElseThrow(() -> new BusinessException(
                    "PROD_001",
                    String.format("Producto no encontrado: ID %d", detalle.productoId()),
                    HttpStatus.NOT_FOUND));

            BigDecimal precioEmpleado = calcularPrecioEmpleado(producto, comedor);
            BigDecimal precioEmpresa = calcularPrecioEmpresa(producto, comedor);

            ConsumoDetalle consumoDetalle = ConsumoDetalle.builder()
                    .consumo(consumo)
                    .producto(producto)
                    .cantidad(detalle.cantidad())
                    .precioUnitarioEmpleado(precioEmpleado)
                    .precioUnitarioEmpresa(precioEmpresa)
                    .build();

            consumoDetalle = consumoDetalleRepository.save(consumoDetalle);

            // Procesar modificadores
            if (detalle.modificadoresIds() != null && !detalle.modificadoresIds().isEmpty()) {
                for (Long modificadorId : detalle.modificadoresIds()) {
                    OpcionModificador modificador = opcionModificadorRepository.findById(modificadorId)
                            .orElseThrow(() -> new BusinessException(
                            "MOD_001",
                            String.format("Modificador no encontrado: ID %d", modificadorId),
                            HttpStatus.NOT_FOUND));

                    ConsumoDetalleModificador consumoMod = ConsumoDetalleModificador.builder()
                            .consumoDetalle(consumoDetalle)
                            .nombreOpcion(modificador.getNombre())
                            .precioExtra(modificador.getPrecioExtra() != null ? modificador.getPrecioExtra() : BigDecimal.ZERO)
                            .build();

                    consumoDetalleModificadorRepository.save(consumoMod);
                }
            }
        }

        // =====================================================
        // 12. DESCONTAR STOCK
        // =====================================================
        for (DetalleRequest detalle : request.detalles()) {
            Producto producto = productoRepository.findById(detalle.productoId()).get();
            if (producto.getControlaInventario() || producto.getControlaPorciones()) {
                inventarioService.descontarStock(
                        detalle.productoId(),
                        comedor.getId(),
                        detalle.cantidad(),
                        consumo
                );
            }
        }

        // =====================================================
        // 13. DETERMINAR ESTADO FINAL
        // =====================================================
        EstadoConsumo estadoFinal;

        if (todosSnacks) {
            // Solo snacks: entregar directamente
            consumo.setEstado(EstadoConsumo.ENTREGADO);
            estadoFinal = EstadoConsumo.ENTREGADO;
            consumo = consumoRepository.save(consumo);
            log.info("Venta directa (snack) - Consumo: {}, Cajero: {}, Empleado: {}, Comedor: {}",
                    consumo.getId(), cajero.getUsername(), empleado.getNumeroEmpleado(), comedor.getNombre());
        } else {
            // Tiene comida: va a cocina (estado PAGADO)
            estadoFinal = EstadoConsumo.PAGADO;
            log.info("Venta directa (comida) - Consumo: {}, Cajero: {}, Empleado: {}, Tipo: {}, Comedor: {}",
                    consumo.getId(), cajero.getUsername(), empleado.getNumeroEmpleado(), tipo.getNombre(), comedor.getNombre());
        }

        // =====================================================
        // 14. REGISTRAR HISTORIAL
        // =====================================================
        registrarHistorial(consumo, estadoFinal, cajero);

        // =====================================================
        // 15. CALCULAR VIGENCIA
        // =====================================================
        String vigencia = calcularVigenciaQR(consumo);

        // =====================================================
        // 16. RETORNAR RESPUESTA
        // =====================================================
        return new PedidoResponse(
                consumo.getId(),
                folio,
                qrToken,
                estadoFinal.name(),
                vigencia
        );
    }

    // =====================================================
    // 1. GENERAR PEDIDO (VERSIÓN SIMPLIFICADA)
    // =====================================================
    /**
     * 📱 Empleado genera un nuevo pedido desde la app
     *
     * FLUJO SIMPLIFICADO: 1. Validaciones de entrada 2. Obtener empleado 3.
     * Determinar tipo de consumo según productos enviados 4. Validar permisos
     * del empleado 5. Validar horario (solo si hay comidas) 6. Validar doble
     * consumo (solo para comidas) 7. Validar límite diario (solo
     * administrativos con comidas) 8. Validar stock (solo snacks) 9. Crear
     * consumo 10. Agregar detalles y modificadores 11. Retornar respuesta
     *
     * @param request DTO con lista de productos seleccionados
     * @param empleadoId ID del empleado autenticado
     * @return PedidoResponse con folio, QR token, estado y vigencia
     */
    @Transactional
    public PedidoResponse generarPedido(CrearConsumoRequest request, Long empleadoId) {

        // =====================================================
        // 1. VALIDACIONES DE ENTRADA
        // =====================================================
        if (request == null) {
            throw new BusinessException("REQ_001", "Solicitud inválida", HttpStatus.BAD_REQUEST);
        }

        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new BusinessException("REQ_002", "Debe seleccionar al menos un producto", HttpStatus.BAD_REQUEST);
        }

        // =====================================================
        // 2. OBTENER EMPLEADO
        // =====================================================
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new BusinessException("EMP_001", "Empleado no encontrado", HttpStatus.NOT_FOUND));

        if (!empleado.getActivo()) {
            throw new BusinessException("EMP_002", "Empleado inactivo", HttpStatus.FORBIDDEN);
        }

        // =====================================================
        // 3. DETERMINAR TIPO DE CONSUMO SEGÚN PRODUCTOS
        // =====================================================
        boolean tieneComida = request.detalles().stream().anyMatch(detalle -> {
            Producto p = productoRepository.findById(detalle.productoId()).get();
            return p.getRequierePreparacion();
        });

        TipoConsumo tipo;
        if (tieneComida) {
            // Hay productos de comida → determinar por hora actual
            LocalTime ahora = LocalTime.now();
            Horario horarioActivo = horarioRepository.findActiveByTime(empleado.getComedor().getId(), ahora)
                    .orElseThrow(() -> new BusinessException(
                    "HOR_002",
                    "No hay servicio activo en este momento",
                    HttpStatus.BAD_REQUEST));
            tipo = horarioActivo.getTipoConsumo();
        } else {
            // Solo snacks → TIENDA
            tipo = tipoConsumoRepository.findById(TIPO_TIENDA_ID)
                    .orElseThrow(() -> new BusinessException(
                    "TIPO_001",
                    "Tipo TIENDA no configurado",
                    HttpStatus.NOT_FOUND));
        }

        // =====================================================
        // 4. VALIDAR PERMISOS DEL EMPLEADO
        // =====================================================
        if (!empleado.puedeConsumir(tipo)) {
            String tiposPermitidos = empleado.getConsumosPermitidos().stream()
                    .map(TipoConsumo::getNombre)
                    .collect(Collectors.joining(", "));

            throw new BusinessException(
                    "EMP_004",
                    String.format("No tienes derecho a %s. Tus permisos: %s",
                            tipo.getNombre(), tiposPermitidos),
                    HttpStatus.FORBIDDEN);
        }

        // =====================================================
        // 5. VALIDAR HORARIO (SOLO SI HAY PRODUCTOS DE COMIDA)
        // =====================================================
        if (tieneComida) {
            validacionService.validarHorario(empleado, tipo, LocalDateTime.now());
        }

        // =====================================================
        // 6. VALIDAR DOBLE CONSUMO (SOLO PARA COMIDAS)
        // =====================================================
        if (tieneComida) {
            validacionService.validarDobleConsumo(empleado.getId(), tipo.getId());
        }

        // =====================================================
        // 7. VALIDAR LÍMITE DIARIO (SOLO ADMINISTRATIVOS CON COMIDAS)
        // =====================================================
        if (tieneComida && !empleado.getHorarioFlexible()) {
            validacionService.validarLimiteDiarioAdministrativo(empleado.getId());
        }

        // =====================================================
        // 8. VALIDAR STOCK (SOLO PARA SNACKS)
        // =====================================================
        for (DetalleRequest detalle : request.detalles()) {
            Producto producto = productoRepository.findById(detalle.productoId()).get();
            if (producto.getControlaInventario()) {
                inventarioService.validarStock(
                        detalle.productoId(),
                        empleado.getComedor().getId(),
                        detalle.cantidad()
                );
            }
        }

        // =====================================================
        // 9. CREAR CONSUMO
        // =====================================================
        String folio = folioService.nextFolioDiario(empleado.getComedor().getNombre());
        String qrToken = UUID.randomUUID().toString();

        Consumo consumo = Consumo.builder()
                .empleado(empleado)
                .comedor(empleado.getComedor())
                .tipoConsumo(tipo)
                .estado(EstadoConsumo.CREADO)
                .tokenQr(qrToken)
                .fecha(LocalDate.now())
                .fechaCreacion(LocalDateTime.now())
                .build();

        consumo = consumoRepository.save(consumo);

        // =====================================================
        // 10. AGREGAR DETALLES DEL PEDIDO
        // =====================================================
        for (DetalleRequest detalle : request.detalles()) {
            Producto producto = productoRepository.findById(detalle.productoId())
                    .orElseThrow(() -> new BusinessException(
                    "PROD_001",
                    String.format("Producto no encontrado: ID %d", detalle.productoId()),
                    HttpStatus.NOT_FOUND));

            BigDecimal precioEmpleado = calcularPrecioEmpleado(producto, empleado.getComedor());
            BigDecimal precioEmpresa = calcularPrecioEmpresa(producto, empleado.getComedor());

            ConsumoDetalle consumoDetalle = ConsumoDetalle.builder()
                    .consumo(consumo)
                    .producto(producto)
                    .cantidad(detalle.cantidad())
                    .precioUnitarioEmpleado(precioEmpleado)
                    .precioUnitarioEmpresa(precioEmpresa)
                    .build();

            consumoDetalle = consumoDetalleRepository.save(consumoDetalle);

            // Procesar modificadores si existen
            if (detalle.modificadoresIds() != null && !detalle.modificadoresIds().isEmpty()) {
                for (Long modificadorId : detalle.modificadoresIds()) {
                    OpcionModificador modificador = opcionModificadorRepository.findById(modificadorId)
                            .orElseThrow(() -> new BusinessException(
                            "MOD_001",
                            String.format("Modificador no encontrado: ID %d", modificadorId),
                            HttpStatus.NOT_FOUND));

                    ConsumoDetalleModificador consumoMod = ConsumoDetalleModificador.builder()
                            .consumoDetalle(consumoDetalle)
                            .nombreOpcion(modificador.getNombre())
                            .precioExtra(modificador.getPrecioExtra() != null ? modificador.getPrecioExtra() : BigDecimal.ZERO)
                            .build();

                    consumoDetalleModificadorRepository.save(consumoMod);
                }
            }
        }

        // =====================================================
        // 11. REGISTRAR HISTORIAL
        // =====================================================
        registrarHistorial(consumo, EstadoConsumo.CREADO, null);

        // =====================================================
        // 12. LOG
        // =====================================================
        log.info("Pedido generado - Folio: {}, Empleado: {}, Tipo: {}, Productos: {}",
                folio,
                empleado.getNumeroEmpleado(),
                tipo.getNombre(),
                request.detalles().size());

        // =====================================================
        // 13. CALCULAR VIGENCIA
        // =====================================================
        String vigencia = calcularVigenciaQR(consumo);

        // =====================================================
        // 14. RETORNAR RESPUESTA
        // =====================================================
        return new PedidoResponse(
                consumo.getId(),
                folio,
                qrToken,
                consumo.getEstado().name(),
                vigencia
        );
    }

    // =====================================================
    // 2. CONSULTAR ESTADO DEL PEDIDO
    // =====================================================
    /**
     * 📱 Consulta el estado del pedido actual del empleado
     *
     * FLUJO: 1. Busca el pedido activo del empleado para hoy 2. Si no existe,
     * retorna estado SIN_PEDIDO 3. Si existe, retorna estado con mensaje
     * amigable
     *
     * @param empleadoId ID del empleado autenticado
     * @return EstadoPedidoResponse con información del pedido
     */
    public EstadoPedidoResponse consultarEstadoPedido(Long empleadoId) {

        // Buscar pedido activo del día
        Optional<Consumo> consumoOpt = consumoRepository.findMiTicketActivo(
                empleadoId,
                LocalDate.now()
        );

        if (consumoOpt.isEmpty()) {
            return new EstadoPedidoResponse(
                    null,
                    null,
                    "SIN_PEDIDO",
                    "No tienes pedidos activos. Genera uno nuevo desde el menú",
                    null,
                    null
            );
        }

        Consumo consumo = consumoOpt.get();

        // Construir respuesta
        String mensaje = getMensajePorEstado(consumo);
        String vigencia = calcularVigenciaQR(consumo);
        String tiempoEstimado = calcularTiempoEstimado(consumo);

        return new EstadoPedidoResponse(
                consumo.getId(),
                consumo.getTokenQr(),
                consumo.getEstado().name(),
                mensaje,
                vigencia,
                tiempoEstimado
        );
    }

    // En ConsumoService.java
    /**
     * 📱 VERIFICACIÓN RÁPIDA: Solo dice si hay pedido activo y su estado.
     *
     * Para consultas muy frecuentes (cada 2-3 segundos). No carga detalles,
     * solo el estado.
     *
     * @param empleadoId ID del empleado
     * @return EstadoPedidoSimple con información mínima
     */
    public EstadoPedidoSimple verificarPedidoActivo(Long empleadoId) {

        Optional<EstadoConsumo> estadoOpt = consumoRepository.findEstadoPedidoActivo(
                empleadoId,
                LocalDate.now()
        );

        if (estadoOpt.isEmpty()) {
            return new EstadoPedidoSimple(null, "SIN_PEDIDO", false);
        }

        EstadoConsumo estado = estadoOpt.get();
        boolean tienePedidoActivo = estado != EstadoConsumo.ENTREGADO
                && estado != EstadoConsumo.CANCELADO;

        return new EstadoPedidoSimple(estado.name(),
                getMensajeCorto(estado),
                tienePedidoActivo);
    }

    /**
     * Mensaje corto para mostrar en la pantalla principal
     */
    private String getMensajeCorto(EstadoConsumo estado) {
        return switch (estado) {
            case CREADO ->
                "QR listo para mostrar";
            case PAGADO ->
                "En espera de cocina";
            case PREPARANDO ->
                "Preparando";
            case LISTO ->
                "¡Listo para recoger!";
            case ENTREGADO ->
                "Disfrutado";
            case CANCELADO ->
                "Cancelado";
            default ->
                "";
        };
    }

    // =====================================================
// EMPLEADO CANCELA SU PROPIO PEDIDO
// =====================================================

/**
 * ❌ Empleado cancela su pedido activo desde la app
 * 
 * FLUJO:
 * 1. Busca pedido activo del empleado (CREADO)
 * 2. Si no hay pedido activo → error
 * 3. Si el pedido ya fue escaneado (PAGADO) → error (debe ir a caja)
 * 4. Cancela el pedido con motivo "Cancelado por el empleado"
 * 5. Libera al empleado para generar un nuevo pedido
 * 
 * @param empleadoId ID del empleado autenticado
 * @return CancelacionResponse con información de la cancelación
 */
@Transactional
public CancelacionResponse cancelarMiPedido(Long empleadoId) {
    
    // Buscar pedido activo del día
    Optional<Consumo> consumoOpt = consumoRepository.findMiTicketActivo(empleadoId, LocalDate.now());
    
    if (consumoOpt.isEmpty()) {
        throw new BusinessException(
                "CON_012",
                "No tienes pedidos activos para cancelar",
                HttpStatus.BAD_REQUEST);
    }
    
    Consumo consumo = consumoOpt.get();
    
    // Solo se puede cancelar si está en estado CREADO (no escaneado)
    if (consumo.getEstado() != EstadoConsumo.CREADO) {
        String mensaje;
        if (consumo.getEstado() == EstadoConsumo.PAGADO) {
            mensaje = "Tu pedido ya fue validado en caja. Acércate al cajero si deseas cancelarlo.";
        } else if (consumo.getEstado() == EstadoConsumo.PREPARANDO) {
            mensaje = "Tu pedido ya está en preparación. No es posible cancelarlo desde la app.";
        } else if (consumo.getEstado() == EstadoConsumo.LISTO) {
            mensaje = "Tu pedido ya está listo para recoger. No es posible cancelarlo desde la app.";
        } else {
            mensaje = "Tu pedido ya fue procesado. No es posible cancelarlo desde la app.";
        }
        
        throw new BusinessException(
                "CON_013",
                mensaje,
                HttpStatus.BAD_REQUEST);
    }
    
    Long pedidoId = consumo.getId();
    
    // Cancelar pedido
    consumo.setEstado(EstadoConsumo.CANCELADO);
    consumo.setMotivoCancelacion("Cancelado por el empleado desde la app");
    consumo.setFechaCancelacion(LocalDateTime.now());
    consumo = consumoRepository.save(consumo);
    
    // Registrar historial (sin usuario, es el empleado)
    registrarHistorial(consumo, EstadoConsumo.CANCELADO, null);
    
    // Log para auditoría
    log.info("Pedido cancelado por empleado - Consumo: {}, Empleado: {}",
            pedidoId, empleadoId);
    
    // Retornar respuesta específica
    return new CancelacionResponse(
            true,
            "✅ Pedido cancelado exitosamente. Puedes generar uno nuevo.",
            pedidoId,
            "Cancelado por el empleado desde la app",
            true
    );
}

    // =====================================================
    // MÉTODOS PRIVADOS DE APOYO
    // =====================================================
    /**
     * Mensaje amigable según estado del pedido
     */
    private String getMensajePorEstado(Consumo consumo) {
        return switch (consumo.getEstado()) {
            case CREADO ->
                String.format("✅ QR generado. Preséntate en caja. Vigencia: %s",
                calcularVigenciaQR(consumo));
            case PAGADO ->
                "🟡 Pedido validado. La cocina lo preparará pronto.";
            case PREPARANDO ->
                "🍳 Tu comida se está preparando.";
            case LISTO ->
                "✅ ¡Tu comida está lista! Pasa a recoger.";
            case ENTREGADO ->
                "🍽️ Disfruta tu comida. ¡Gracias!";
            case CANCELADO ->
                "❌ Pedido cancelado. Consulta con el cajero si tienes dudas.";
            default ->
                "Estado desconocido";
        };
    }

    /**
     * Calcula la vigencia del QR según el tipo de consumo
     */
    private String calcularVigenciaQR(Consumo consumo) {
        if (TIPO_TIENDA_ID.equals(consumo.getTipoConsumo().getId())) {
            return "30 minutos desde ahora";
        }

        Optional<Horario> horario = horarioRepository
                .findByComedorIdAndTipoConsumoIdAndActivoTrue(
                        consumo.getComedor().getId(),
                        consumo.getTipoConsumo().getId());

        return horario.map(h -> "Hasta las " + h.getHoraFin())
                .orElse("No definida");
    }

    /**
     * Calcula tiempo estimado de espera (solo para estados PREPARANDO y LISTO)
     */
    private String calcularTiempoEstimado(Consumo consumo) {
        if (consumo.getEstado() == EstadoConsumo.PREPARANDO) {
            // Calcular cantidad total de productos
            int cantidadProductos = consumo.getDetalles().stream()
                    .mapToInt(ConsumoDetalle::getCantidad)
                    .sum();

            if (cantidadProductos <= 2) {
                return "5-7 minutos";
            } else if (cantidadProductos <= 4) {
                return "8-10 minutos";
            } else {
                return "10-15 minutos";
            }
        }

        if (consumo.getEstado() == EstadoConsumo.LISTO) {
            return "Listo ahora";
        }

        return null;
    }

    // =====================================================
    // OTROS MÉTODOS (calcularPrecioEmpleado, etc.)
    // =====================================================
    private BigDecimal calcularPrecioEmpleado(Producto producto, Comedor comedor) {
        return comedorProductoRepository
                .findByComedorIdAndProductoId(comedor.getId(), producto.getId())
                .map(ComedorProducto::getPrecioEmpleado)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calcularPrecioEmpresa(Producto producto, Comedor comedor) {
        return comedorProductoRepository
                .findByComedorIdAndProductoId(comedor.getId(), producto.getId())
                .map(ComedorProducto::getPrecioEmpresa)
                .orElse(BigDecimal.ZERO);
    }

    private void registrarHistorial(Consumo consumo, EstadoConsumo estado, Usuario usuario) {
        ConsumoEstadoHistorial historial = ConsumoEstadoHistorial.builder()
                .consumo(consumo)
                .estado(estado)
                .usuario(usuario)
                .build();
        consumoEstadoHistorialRepository.save(historial);
    }
}
