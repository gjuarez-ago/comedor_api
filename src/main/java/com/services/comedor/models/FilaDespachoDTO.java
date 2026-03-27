package com.services.comedor.models;

import java.util.List;

public record FilaDespachoDTO(
        Long pedidoId,
        String folio,
        String nombreEmpleado,
        String estado,
        List<ResumenPlatilloDTO> items
) {}

