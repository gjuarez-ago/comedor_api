package com.services.comedor.exception;

public final class ErrorCodes {

    private ErrorCodes() {
        // Evita instanciación
    }

    // ============================================
    // CÓDIGOS DE ERROR (Para el Frontend / App)
    // ============================================

    // 🔐 Errores de Autenticación (AUTH-XXX)
    public static final String AUTH_NOT_FOUND = "AUTH-001";
    public static final String AUTH_SUSPENDED = "AUTH-002";
    public static final String AUTH_INVALID_PIN = "AUTH-003";
    public static final String AUTH_UNAUTHORIZED_LOCATION = "AUTH-004"; // Intenta loguearse en otro comedor
    public static final String AUTH_INVALID_SUPERVISOR = "AUTH-005"; // PIN de jefe inválido

    // 👥 Errores de Empleado (EMP-XXX)
    public static final String EMP_NOT_FOUND = "EMP-001";
    public static final String EMP_INACTIVE = "EMP-002";

    // 🍽️ Errores de Menú y Turnos (MENU-XXX)
    public static final String MENU_CLOSED_SHIFT = "MENU-001"; // Fuera de horario
    public static final String MENU_EMPTY = "MENU-002";
    public static final String MENU_MODIFIER_INVALID = "MENU-003"; // Modificador hackeado/inválido

    // 🛒 Errores de Consumo / Tickets (CONS-XXX)
    public static final String CONS_NOT_FOUND = "CONS-001";
    public static final String CONS_ALREADY_ATE = "CONS-002"; // 🔥 El famoso "Candado Anti-Fraude"
    public static final String CONS_QR_ALREADY_USED = "CONS-003"; // QR ya fue procesado en caja
    public static final String CONS_WRONG_LOCATION = "CONS-004"; // El QR es de otra sucursal

    // 📦 Errores de Inventario (STK-XXX)
    public static final String STK_OUT_OF_STOCK = "STK-001";

    // 🍳 Errores de Cocina y Flujo (KDS-XXX / FLOW-XXX)
    public static final String FLOW_INVALID_TRANSITION = "FLOW-001"; // Ej: Pasar a ENTREGADO sin estar LISTO
    public static final String FLOW_CANNOT_CANCEL = "FLOW-002"; // Ya se entregó, no se puede cancelar


    // ============================================
    // MENSAJES DESCRIPTIVOS (Para Logs o Mostrar al Usuario)
    // ============================================

    // Autenticación
    public static final String MSG_AUTH_NOT_FOUND = "Usuario o número de empleado no encontrado.";
    public static final String MSG_AUTH_SUSPENDED = "Cuenta suspendida. Acude a Recursos Humanos.";
    public static final String MSG_AUTH_INVALID_PIN = "El PIN ingresado es incorrecto.";
    public static final String MSG_AUTH_UNAUTHORIZED_LOCATION = "No tienes permisos para operar en esta sucursal.";
    public static final String MSG_AUTH_INVALID_SUPERVISOR = "PIN de autorización de Jefe inválido o sin privilegios.";

    // Empleado
    public static final String MSG_EMP_NOT_FOUND = "El empleado solicitado no existe.";
    public static final String MSG_EMP_INACTIVE = "El empleado se encuentra inactivo en el sistema.";

    // Menú
    public static final String MSG_MENU_CLOSED_SHIFT = "El comedor se encuentra cerrado en este horario.";
    public static final String MSG_MENU_EMPTY = "No hay platillos configurados para este turno.";
    public static final String MSG_MENU_MODIFIER_INVALID = "La opción o modificador seleccionado no es válido para este platillo.";

    // Consumo / Tickets
    public static final String MSG_CONS_NOT_FOUND = "El pedido o ticket solicitado no existe.";
    public static final String MSG_CONS_ALREADY_ATE = "Ya registraste un consumo en este turno. No puedes generar otro ticket.";
    public static final String MSG_CONS_QR_ALREADY_USED = "Este código QR ya fue procesado y entregado.";
    public static final String MSG_CONS_WRONG_LOCATION = "Este ticket pertenece a otro comedor y no puede canjearse aquí.";

    // Inventario
    public static final String MSG_STK_OUT_OF_STOCK = "Lo sentimos, no hay stock suficiente para: %s"; // Soporta String.format

    // Cocina y Flujo
    public static final String MSG_FLOW_INVALID_TRANSITION = "Transición de estado no permitida. Verifica el estado actual del pedido.";
    public static final String MSG_FLOW_CANNOT_CANCEL = "El pedido ya fue entregado y no puede ser cancelado.";
}