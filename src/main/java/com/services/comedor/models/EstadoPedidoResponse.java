package com.services.comedor.models;

/**
 * Respuesta para consultar el estado del pedido actual del empleado
 * 
 * @param pedidoId ID del pedido
 * @param qrToken Token QR del pedido
 * @param estado Estado actual (CREADO, PAGADO, PREPARANDO, LISTO, ENTREGADO, CANCELADO)
 * @param mensaje Mensaje amigable para el empleado
 * @param vigencia Vigencia del QR (si aplica)
 * @param tiempoEstimado Tiempo estimado de espera (si aplica)
 */
public record EstadoPedidoResponse(
    Long pedidoId,
    String qrToken,
    String estado,
    String mensaje,
    String vigencia,
    String tiempoEstimado
) {}