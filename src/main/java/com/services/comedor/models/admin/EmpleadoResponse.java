package com.services.comedor.models.admin;

import java.util.Set;

public record EmpleadoResponse(
        Long id,
        String numeroEmpleado,
        String nombre,
        String telefono,
        Long comedorId,
        String comedorNombre,
        Boolean horarioFlexible,
        Set<TipoConsumoResponse> tiposConsumoPermitidos,
        Boolean activo
) {}

