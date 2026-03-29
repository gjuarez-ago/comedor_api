package com.services.comedor.models.admin;

import java.util.List;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        String imagenUrl,
        Boolean requierePreparacion,
        Boolean controlaInventario,
        Boolean controlaPorciones,
        Boolean activo,
        List<ConfiguracionComedorResponse> configuracionComedores
) {}

