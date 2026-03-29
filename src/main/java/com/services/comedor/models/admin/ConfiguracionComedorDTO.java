package com.services.comedor.models.admin;

import java.math.BigDecimal;
import java.util.Set;

public record ConfiguracionComedorDTO(
        Long comedorId,
        BigDecimal precioEmpleado,
        BigDecimal precioEmpresa,
        Boolean disponible,
        Set<Long> turnosDisponiblesIds,
        Set<Integer> diasDisponibles
) {}

