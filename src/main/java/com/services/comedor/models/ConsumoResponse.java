package com.services.comedor.models;

/**
 * Respuesta base para operaciones de consumo (entregar, cancelar)
 * 
 * Para validación QR se usa {@link ValidacionQRResponse} que incluye
 * más detalles como productos y tipo de entrega.
 * 
 * @param id ID del consumo/pedido
 * @param qrToken Token QR del pedido
 * @param estado Estado actual (CREADO, PAGADO, PREPARANDO, LISTO, ENTREGADO, CANCELADO)
 */
public record ConsumoResponse(
    Long id,
    String qrToken,
    String estado
) {}