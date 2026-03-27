package com.services.comedor.models;

import java.util.Set;

public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precioBase,
        String imagenUrl,
        Set<GrupoModificadorDTO> gruposModificadores
) {}

