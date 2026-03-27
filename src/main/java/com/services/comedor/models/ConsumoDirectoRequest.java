package com.services.comedor.models;

import java.util.List;

public record ConsumoDirectoRequest(
        Long empleadoId,
        Long comedorId,
        String pinJefe,
        String motivo,
        List<DetalleRequest> detalles
) {}

