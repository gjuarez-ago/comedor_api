package com.services.comedor.models.admin;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CrearHorarioRequest(
        @NotNull(message = "El comedor es requerido")
        Long comedorId,
        @NotNull(message = "El tipo de consumo es requerido")
        Long tipoConsumoId,
        @NotNull(message = "El dia de semana es requerido")
        Integer diaSemana,
        @NotNull(message = "La hora de inicio es requerida")
        LocalTime horaInicio,
        @NotNull(message = "La hora de fin es requerida")
        LocalTime horaFin,
        Boolean activo
) {}
