package com.services.comedor.models;

/**
 * Respuesta específica para cancelación de pedido
 * 
 * @param cancelado true si se canceló exitosamente
 * @param mensaje Mensaje amigable para el usuario
 * @param pedidoAnteriorId ID del pedido cancelado (si aplica)
 * @param motivo Motivo de cancelación registrado
 * @param puedeGenerarNuevo true si el empleado puede generar otro pedido
 */
public record CancelacionResponse(
        Boolean cancelado,
        String mensaje,
        Long pedidoAnteriorId,
        String motivo,
        Boolean puedeGenerarNuevo
) {}