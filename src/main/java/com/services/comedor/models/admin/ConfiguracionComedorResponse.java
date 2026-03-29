package com.services.comedor.models.admin;

import java.math.BigDecimal;
import java.util.Set;

public record ConfiguracionComedorResponse(
        Long comedorId,
        String comedorNombre,
        BigDecimal precioEmpleado,
        BigDecimal precioEmpresa,
        Boolean disponible,
        Set<TipoConsumoResponse> turnosDisponibles,
        Set<Integer> diasDisponibles
) {}

