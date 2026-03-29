package com.services.comedor.models;

/**
 * DTO ultraligero para consulta rápida de estado del pedido
 * 
 * @param estado Estado actual (CREADO, PAGADO, PREPARANDO, LISTO, etc.)
 * @param mensaje Mensaje corto para mostrar en pantalla
 * @param tienePedidoActivo true si hay pedido en curso (no entregado ni cancelado)
 */
public record EstadoPedidoSimple(
    String estado,
    String mensaje,
    Boolean tienePedidoActivo
) {}