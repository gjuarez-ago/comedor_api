package com.services.comedor.models.admin;

import java.time.LocalTime;

public record HorarioResponse(
        Long id,
        Long comedorId,
        String comedorNombre,
        Long tipoConsumoId,
        String tipoConsumoNombre,
        Integer diaSemana,
        String diaNombre,
        LocalTime horaInicio,
        LocalTime horaFin,
        Boolean activo
) {}

