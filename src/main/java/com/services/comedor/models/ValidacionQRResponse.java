package com.services.comedor.models;

import java.util.List;

/**
 * Respuesta detallada después de validar un QR en caja
 * 
 * @param consumoId ID del consumo procesado
 * @param qrToken Token QR del pedido
 * @param estado Estado final (ENTREGADO o PAGADO)
 * @param esEntregaRapida true = snack (entrega directa), false = comida (va a cocina)
 * @param requierePreparacion true si algún producto necesita cocina
 * @param productos Lista de productos validados con detalles
 */
public record ValidacionQRResponse(
        Long consumoId,
        String qrToken,
        String estado,
        Boolean esEntregaRapida,
        Boolean requierePreparacion,
        List<ProductoValidadoDTO> productos
) {}
