package com.services.comedor.models;

/**
 * Respuesta después de generar un pedido
 * 
 * @param id ID del consumo generado
 * @param folio Folio único del pedido (ej: "NORTE-260327-0045")
 * @param qrToken Token único para generar el QR
 * @param estado Estado actual del pedido (CREADO, PAGADO, etc.)
 * @param vigencia Descripción de la vigencia del QR (ej: "Hasta las 09:00" o "30 minutos")
 */
public record PedidoResponse(
    Long id,
    String folio,
    String qrToken,
    String estado,
    String vigencia
) {}