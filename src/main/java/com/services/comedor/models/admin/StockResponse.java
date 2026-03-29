package com.services.comedor.models.admin;

public record StockResponse(
        Long productoId,
        String productoNombre,
        Long comedorId,
        String comedorNombre,
        Integer stockActual,
        Boolean controlaStock
) {}

