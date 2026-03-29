package com.services.comedor.models.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarStockRequest(
        @NotNull(message = "El producto es requerido")
        Long productoId,
        @NotNull(message = "El comedor es requerido")
        Long comedorId,
        @NotNull(message = "La cantidad es requerida")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        Integer cantidad,
        String motivo
) {}

