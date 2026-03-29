package com.services.comedor.models;

import java.time.LocalDateTime;

import com.services.comedor.enums.EstadoConsumo;

/**
 * DTO para resumen de consumo (sin cargar entidades completas)
 * Útil para reportes y archivado
 */
public record ConsumoResumenDTO(
        Long id,
        String qrToken,
        EstadoConsumo estado,
        LocalDateTime fechaCreacion,
        Long empleadoId,
        String empleadoNombre,
        String empleadoNumero,
        Long tipoConsumoId,
        String tipoConsumoNombre
) {
}