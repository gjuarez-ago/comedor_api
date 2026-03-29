package com.services.comedor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.services.comedor.models.CancelacionResponse;
import com.services.comedor.models.ComandaKdsResponse;
import com.services.comedor.models.ConsumoDirectoRequest;
import com.services.comedor.models.ConsumoResponse;
import com.services.comedor.models.CrearConsumoRequest;
import com.services.comedor.models.EmpleadoDTO;
import com.services.comedor.models.EstadoPedidoResponse;
import com.services.comedor.models.EstadoPedidoSimple;
import com.services.comedor.models.PanelDespachoResponse;
import com.services.comedor.models.PantallaTvResponse;
import com.services.comedor.models.PedidoResponse;
import com.services.comedor.models.TicketTvDTO;
import com.services.comedor.models.ValidacionQRResponse;
import com.services.comedor.services.CajaService;
import com.services.comedor.services.CocinaService;
import com.services.comedor.services.ConsumoService;
import com.services.comedor.services.EmpleadoAdminService;
import com.services.comedor.services.PanelDespachoService;
import com.services.comedor.services.PantallaTvService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador principal para la aplicación móvil y operaciones de comedor
 *
 * Este controller agrupa todos los endpoints necesarios para: - App del
 * empleado (generar pedido, consultar estado) - Pantalla de cocina (KDS) -
 * Pantalla de TV pública - Panel de despacho - Operaciones de caja (validar QR,
 * entregar, cancelar, venta directa)
 */
@RestController
@RequestMapping("/api/movil")
@RequiredArgsConstructor
public class MovilController {

    private final PanelDespachoService panelDespachoService;
    private final ConsumoService consumoService;
    private final CocinaService cocinaService;
    private final EmpleadoAdminService empleadoService;
    private final PantallaTvService pantallaTvService;
    private final CajaService cajaService;

    // =====================================================
    // 📱 EMPLEADO - GENERAR PEDIDO
    // =====================================================
    /**
     * 📱 Empleado genera un nuevo pedido desde la app móvil
     *
     * El sistema determina automáticamente el tipo de consumo según los
     * productos seleccionados: - Si todos son snacks → Tipo TIENDA - Si hay
     * algún producto de comida → Tipo según hora actual (DESAYUNO/COMIDA/CENA)
     *
     * URL: POST /api/movil/crear-pedido
     *
     * @param request Datos del pedido con lista de productos y modificadores
     * @param empleadoId ID del empleado autenticado (del token JWT)
     * @return PedidoResponse con QR token, folio y vigencia
     *
     * @example Request POST /api/movil/crear-pedido Headers: X-Empleado-Id: 1
     * Content-Type: application/json { "detalles": [ { "productoId": 4,
     * "cantidad": 1, "modificadoresIds": [101, 102] }, { "productoId": 13,
     * "cantidad": 1, "modificadoresIds": [] } ] }
     *
     * @example Response (éxito) { "id": 100, "folio": "NORTE-280328-0045",
     * "qrToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "CREADO",
     * "vigencia": "Hasta las 15:00" }
     *
     * @example Response (error - fuera de horario) { "timestamp":
     * "2026-03-28T10:30:00", "status": 400, "error": "Bad Request", "code":
     * "HOR_002", "message": "No hay servicio activo en este momento. Solo
     * puedes pedir snacks.", "path": "/api/movil/crear-pedido" }
     */
    @PostMapping("/crear-pedido")
    public ResponseEntity<PedidoResponse> generarPedido(
            @Valid @RequestBody CrearConsumoRequest request,
            @RequestHeader("X-Empleado-Id") Long empleadoId) {

        PedidoResponse response = consumoService.generarPedido(request, empleadoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================
    // 📱 EMPLEADO - CONSULTAR ESTADO DEL PEDIDO
    // =====================================================
    /**
     * 📱 Empleado consulta el estado detallado de su pedido actual
     *
     * Retorna información completa del pedido incluyendo mensaje amigable,
     * vigencia del QR y tiempo estimado de espera.
     *
     * URL: GET /api/movil/estado
     *
     * @param empleadoId ID del empleado autenticado (del token JWT)
     * @return EstadoPedidoResponse con estado y mensaje amigable
     *
     * @example Request GET /api/movil/estado Headers: X-Empleado-Id: 1
     *
     * @example Response (sin pedido activo) { "pedidoId": null, "qrToken":
     * null, "estado": "SIN_PEDIDO", "mensaje": "No tienes pedidos activos.
     * Genera uno nuevo desde el menú", "vigencia": null, "tiempoEstimado": null
     * }
     *
     * @example Response (pedido en PREPARANDO) { "pedidoId": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "PREPARANDO",
     * "mensaje": "🍳 Tu comida se está preparando.", "vigencia": null,
     * "tiempoEstimado": "5-7 minutos" }
     *
     * @example Response (pedido LISTO) { "pedidoId": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "LISTO", "mensaje": "✅
     * ¡Tu comida está lista! Pasa a recoger.", "vigencia": null,
     * "tiempoEstimado": "Listo ahora" }
     */
    @GetMapping("/estado")
    public ResponseEntity<EstadoPedidoResponse> consultarEstadoPedido(
            @RequestHeader("X-Empleado-Id") Long empleadoId) {

        EstadoPedidoResponse response = consumoService.consultarEstadoPedido(empleadoId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 📱 EMPLEADO - VERIFICACIÓN RÁPIDA
    // =====================================================
    /**
     * 📱 VERIFICACIÓN RÁPIDA - Consulta ultraligera para la app
     *
     * Ideal para consultar cada 2-3 segundos. Solo retorna si hay pedido activo
     * y su estado, sin cargar detalles pesados.
     *
     * URL: GET /api/movil/verificar
     *
     * @param empleadoId ID del empleado autenticado
     * @return EstadoPedidoSimple con información mínima
     *
     * @example Request GET /api/movil/verificar Headers: X-Empleado-Id: 1
     *
     * @example Response (sin pedido) { "estado": null, "mensaje": "SIN_PEDIDO",
     * "tienePedidoActivo": false }
     *
     * @example Response (con pedido en PREPARANDO) { "estado": "PREPARANDO",
     * "mensaje": "Preparando", "tienePedidoActivo": true }
     */
    @GetMapping("/verificar")
    public ResponseEntity<EstadoPedidoSimple> verificarPedidoActivo(
            @RequestHeader("X-Empleado-Id") Long empleadoId) {

        EstadoPedidoSimple response = consumoService.verificarPedidoActivo(empleadoId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 📱 BUSCAR EMPLEADOS (para venta directa)
    // =====================================================
    /**
     * Busca empleados por número de empleado, nombre o teléfono
     *
     * Útil para autocompletado en la venta directa.
     *
     * URL: GET /api/movil/buscar?q=CAM001
     *
     * @param termino Texto a buscar (número de empleado, nombre o teléfono)
     * @return Lista de empleados que coinciden (máx 10 resultados)
     *
     * @example Request GET /api/movil/buscar?q=CAM
     *
     * @example Response [ { "id": 1, "numeroEmpleado": "CAM001", "nombre":
     * "Pedro González", "telefono": "5512345701", "comedorNombre": "COMEDOR
     * NORTE", "horarioFlexible": true }, { "id": 2, "numeroEmpleado": "CAM002",
     * "nombre": "José Martínez", "telefono": "5512345702", "comedorNombre":
     * "COMEDOR NORTE", "horarioFlexible": true } ]
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<EmpleadoDTO>> buscarEmpleados(
            @RequestParam("q") String termino) {

        List<EmpleadoDTO> resultados = empleadoService.buscarEmpleados(termino);
        return ResponseEntity.ok(resultados);
    }

    // =====================================================
    // 🍳 COCINA - VER PEDIDOS PENDIENTES (KDS)
    // =====================================================
    /**
     * 🍳 Obtiene pedidos para pantalla de cocina (KDS)
     *
     * Retorna pedidos en estado PAGADO (pendientes) y PREPARANDO (en proceso).
     * Los pedidos LISTO no aparecen (pasan al despachador).
     *
     * URL: GET /api/movil/pedidos/{comedorId}
     *
     * @param comedorId ID del comedor
     * @return Lista de pedidos para cocina con detalles de productos y
     * modificadores
     *
     * @example Request GET /api/movil/pedidos/1
     *
     * @example Response [ { "pedidoId": 100, "folio": "NORTE-280328-0044",
     * "hora": "13:25", "items": [ { "productoNombre": "COMIDA CORRIENTE",
     * "cantidad": 1, "notas": [ { "nombre": "Sin cebolla" } ] } ] } ]
     */
    @GetMapping("/pedidos/{comedorId}")
    public ResponseEntity<List<ComandaKdsResponse>> verPedidosPendientes(
            @PathVariable Long comedorId) {

        List<ComandaKdsResponse> response = cocinaService.verPedidosPendientes(comedorId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 🍳 COCINA - MARCAR COMO PREPARANDO
    // =====================================================
    /**
     * 🍳 Cocinero toma un pedido para empezar a cocinar
     *
     * Cambia estado de PAGADO → PREPARANDO
     *
     * URL: POST /api/movil/preparar/{consumoId}
     *
     * @param consumoId ID del pedido
     * @param usuarioId ID del cocinero autenticado
     * @return ConsumoResponse con estado actualizado
     *
     * @example Request POST /api/movil/preparar/100 Headers: X-Usuario-Id: 2
     *
     * @example Response { "id": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "PREPARANDO" }
     */
    @PostMapping("/preparar/{consumoId}")
    public ResponseEntity<ConsumoResponse> marcarPreparando(
            @PathVariable Long consumoId,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        ConsumoResponse response = cocinaService.marcarPreparando(consumoId, usuarioId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 🍳 COCINA - MARCAR COMO LISTO
    // =====================================================
    /**
     * 🍳 Cocinero termina de preparar el pedido
     *
     * Cambia estado de PREPARANDO → LISTO El pedido desaparece de la pantalla
     * de cocina y aparece en el panel de despacho.
     *
     * URL: POST /api/movil/listo/{consumoId}
     *
     * @param consumoId ID del pedido
     * @param usuarioId ID del cocinero autenticado
     * @return ConsumoResponse con estado actualizado
     *
     * @example Request POST /api/movil/listo/100 Headers: X-Usuario-Id: 2
     *
     * @example Response { "id": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "LISTO" }
     */
    @PostMapping("/listo/{consumoId}")
    public ResponseEntity<ConsumoResponse> marcarListo(
            @PathVariable Long consumoId,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        ConsumoResponse response = cocinaService.marcarListo(consumoId, usuarioId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 📺 TV PÚBLICA - PANTALLA DE PEDIDOS LISTOS
    // =====================================================
    /**
     * Obtiene pantalla TV pública con pedidos listos para recoger
     *
     * Muestra en una TV los folios de pedidos en estado LISTO.
     *
     * URL: GET /api/movil/pantalla/{comedorId}
     *
     * @param comedorId ID del comedor
     * @return PantallaTvResponse con pedidos listos
     *
     * @example Request GET /api/movil/pantalla/1
     *
     * @example Response { "enPreparacion": [], "listosParaRecoger": [ {
     * "folio": "NORTE-280328-0044", "estado": "LISTO" }, { "folio":
     * "NORTE-280328-0045", "estado": "LISTO" } ] }
     */
    @GetMapping("/pantalla/{comedorId}")
    public ResponseEntity<PantallaTvResponse> obtenerPantallaTv(
            @PathVariable Long comedorId) {

        PantallaTvResponse response = pantallaTvService.obtenerPantallaTv(comedorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene solo pedidos listos (versión simplificada para TV)
     *
     * URL: GET /api/movil/listos/{comedorId}
     *
     * @param comedorId ID del comedor
     * @return Lista de tickets listos
     *
     * @example Request GET /api/movil/listos/1
     *
     * @example Response [ { "folio": "NORTE-280328-0044", "estado": "LISTO" },
     * { "folio": "NORTE-280328-0045", "estado": "LISTO" } ]
     */
    @GetMapping("/listos/{comedorId}")
    public ResponseEntity<List<TicketTvDTO>> obtenerPedidosListos(
            @PathVariable Long comedorId) {

        List<TicketTvDTO> response = pantallaTvService.obtenerPedidosListos(comedorId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 📊 DESPACHO - PANEL DEL CAJERO
    // =====================================================
    /**
     * Obtiene panel del despachador (cajero) con pedidos ordenados por
     * prioridad
     *
     * Orden: 1. LISTO (prioridad máxima - para entregar ya) 2. PREPARANDO 3.
     * PAGADO 4. CREADO
     *
     * URL: GET /api/movil/panel/{comedorId}
     *
     * @param comedorId ID del comedor
     * @return PanelDespachoResponse con pedidos agrupados por prioridad
     *
     * @example Request GET /api/movil/panel/1
     *
     * @example Response { "prioridadAlta": [ { "pedidoId": 100, "folio":
     * "NORTE-280328-0044", "nombreEmpleado": "Pedro González",
     * "numeroEmpleado": "CAM001", "estado": "LISTO", "items": [ { "nombre":
     * "COMIDA CORRIENTE", "cantidad": 1, "notas": ["Sin cebolla"] } ],
     * "tiempoEspera": "15 min" } ], "enEspera": [ { "pedidoId": 101, "folio":
     * "NORTE-280328-0045", "nombreEmpleado": "María López", "numeroEmpleado":
     * "ADM001", "estado": "PREPARANDO", "items": [ { "nombre": "MOLE POBLANO",
     * "cantidad": 1, "notas": [] } ], "tiempoEspera": "8 min" } ] }
     */
    @GetMapping("/panel/{comedorId}")
    public ResponseEntity<PanelDespachoResponse> obtenerPanelDespacho(
            @PathVariable Long comedorId) {

        PanelDespachoResponse response = panelDespachoService.obtenerPanelDespacho(comedorId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 💳 CAJA - VALIDAR QR
    // =====================================================
    /**
     * 💳 Cajero escanea el QR del empleado para validar el pedido
     *
     * FLUJO: - Snack: descuenta stock → ENTREGADO (entrega inmediata) - Comida:
     * descuenta stock (si aplica) → PAGADO (va a cocina)
     *
     * URL: POST /api/movil/validar/{token}
     *
     * @param token QR token escaneado
     * @param usuarioId ID del cajero autenticado
     * @return ValidacionQRResponse con detalles de la validación
     *
     * @example Request POST
     * /api/movil/validar/a1b2c3d4-e5f6-7890-abcd-ef1234567890 Headers:
     * X-Usuario-Id: 1
     *
     * @example Response (snack) { "consumoId": 101, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "ENTREGADO",
     * "esEntregaRapida": true, "requierePreparacion": false, "productos": [ {
     * "productoId": 13, "nombre": "GALLETA EMPERADOR", "cantidad": 2,
     * "modificadores": [], "controlaStock": true, "stockRestante": 48 } ] }
     *
     * @example Response (comida) { "consumoId": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "PAGADO",
     * "esEntregaRapida": false, "requierePreparacion": true, "productos": [ {
     * "productoId": 4, "nombre": "COMIDA CORRIENTE", "cantidad": 1,
     * "modificadores": ["Sin cebolla"], "controlaStock": true, "stockRestante":
     * 49 } ] }
     */
    @PostMapping("/validar/{token}")
    public ResponseEntity<ValidacionQRResponse> validarQR(
            @PathVariable String token,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        ValidacionQRResponse response = cajaService.validarQR(token, usuarioId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 💳 CAJA - ENTREGAR PEDIDO
    // =====================================================
    /**
     * 🍽️ Cajero entrega el pedido al empleado
     *
     * Solo aplica para COMIDAS que ya están LISTO. Los snacks se entregan
     * directamente en validarQR.
     *
     * URL: POST /api/movil/entregar/{consumoId}
     *
     * @param consumoId ID del pedido a entregar
     * @param usuarioId ID del cajero autenticado
     * @return ConsumoResponse con estado ENTREGADO
     *
     * @example Request POST /api/movil/entregar/100 Headers: X-Usuario-Id: 1
     *
     * @example Response { "id": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "ENTREGADO" }
     */
    @PostMapping("/entregar/{consumoId}")
    public ResponseEntity<ConsumoResponse> entregarPedido(
            @PathVariable Long consumoId,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        ConsumoResponse response = cajaService.entregarPedido(consumoId, usuarioId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 💳 CAJA - CANCELAR PEDIDO
    // =====================================================
    /**
     * ❌ Cancela un pedido (error humano, falta de ingredientes, etc.)
     *
     * Dependiendo del estado: - Si estaba PAGADO y controla stock → devuelve
     * stock - Si estaba PREPARANDO o LISTO → marca como merma
     *
     * URL: POST /api/movil/cancelar/{consumoId}?motivo=...
     *
     * @param consumoId ID del pedido a cancelar
     * @param motivo Razón de la cancelación
     * @param usuarioId ID del usuario que cancela
     * @return ConsumoResponse con estado CANCELADO
     *
     * @example Request POST
     * /api/movil/cancelar/100?motivo=Cliente%20no%20lleg%C3%B3 Headers:
     * X-Usuario-Id: 1
     *
     * @example Response { "id": 100, "qrToken":
     * "a1b2c3d4-e5f6-7890-abcd-ef1234567890", "estado": "CANCELADO" }
     */
    @PostMapping("/cancelar/{consumoId}")
    public ResponseEntity<ConsumoResponse> cancelarPedido(
            @PathVariable Long consumoId,
            @RequestParam String motivo,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        ConsumoResponse response = cajaService.cancelarPedido(consumoId, usuarioId, motivo);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 💳 CAJA - VENTA DIRECTA (MOSTRADOR)
    // =====================================================
    /**
     * 📱 Venta directa - Para empleados sin celular
     *
     * El cajero crea el pedido directamente cuando el empleado no tiene
     * celular.
     *
     * FLUJO: 1. Cajero ingresa número de empleado 2. Cajero selecciona
     * productos 3. Sistema valida todo 4. Si solo snacks → ENTREGADO directo 5.
     * Si hay comida → PAGADO (va a cocina)
     *
     * URL: POST /api/movil/directo
     *
     * @param request Datos de la venta (número de empleado, comedor y
     * productos)
     * @param usuarioId ID del cajero autenticado
     * @return PedidoResponse con folio, QR token y estado
     *
     * @example Request POST /api/movil/directo Headers: X-Usuario-Id: 1
     * Content-Type: application/json { "numeroEmpleado": "CAM001", "comedorId":
     * 1, "detalles": [ { "productoId": 13, "cantidad": 2, "modificadoresIds":
     * [] } ] }
     *
     * @example Response (snack) { "id": 101, "folio": "NORTE-280328-0046",
     * "qrToken": "xyz-789-ghi-012", "estado": "ENTREGADO", "vigencia": "30
     * minutos desde ahora" }
     *
     * @example Response (comida) { "id": 102, "folio": "NORTE-280328-0047",
     * "qrToken": "def-456-ghi-789", "estado": "PAGADO", "vigencia": "Hasta las
     * 15:00" }
     */
    @PostMapping("/directo")
    public ResponseEntity<PedidoResponse> ventaDirecta(
            @Valid @RequestBody ConsumoDirectoRequest request,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        PedidoResponse response = consumoService.ventaDirecta(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================
// ❌ EMPLEADO CANCELA SU PROPIO PEDIDO
// =====================================================
    /**
     * ❌ Empleado cancela su pedido activo desde la app
     *
     * Permite al empleado cancelar su pedido mientras esté en estado CREADO. Si
     * ya fue escaneado en caja (PAGADO, PREPARANDO, LISTO), debe acudir al
     * cajero.
     *
     * URL: POST /api/movil/cancelar-pedido
     *
     * @param empleadoId ID del empleado autenticado (del token JWT)
     * @return CancelacionResponse con información de la cancelación
     *
     * @example Request POST /api/movil/cancelar-pedido Headers: X-Empleado-Id:
     * 1
     *
     * @example Response (éxito) { "cancelado": true, "mensaje": "✅ Pedido
     * cancelado exitosamente. Puedes generar uno nuevo.", "pedidoAnteriorId":
     * 100, "motivo": "Cancelado por el empleado desde la app",
     * "puedeGenerarNuevo": true }
     *
     * @example Response (error - no hay pedido) { "timestamp":
     * "2026-03-28T13:30:00", "status": 400, "error": "Bad Request", "code":
     * "CON_012", "message": "No tienes pedidos activos para cancelar", "path":
     * "/api/movil/cancelar-pedido" }
     *
     * @example Response (error - pedido ya procesado) { "timestamp":
     * "2026-03-28T13:30:00", "status": 400, "error": "Bad Request", "code":
     * "CON_013", "message": "Tu pedido ya fue validado en caja. Acércate al
     * cajero si deseas cancelarlo.", "path": "/api/movil/cancelar-pedido" }
     */
    @PostMapping("/cancelar-pedido")
    public ResponseEntity<CancelacionResponse> cancelarMiPedido(
            @RequestHeader("X-Empleado-Id") Long empleadoId) {

        CancelacionResponse response = consumoService.cancelarMiPedido(empleadoId);
        return ResponseEntity.ok(response);
    }

}
