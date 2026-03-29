package com.services.comedor.models.admin;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CopiarHorariosRequest(
        @NotNull(message = "El comedor es requerido")
        Long comedorId,
        @NotNull(message = "El dia origen es requerido")
        Integer diaOrigen,
        @NotNull(message = "Los dias destino son requeridos")
        List<Integer> diasDestino
) {}
