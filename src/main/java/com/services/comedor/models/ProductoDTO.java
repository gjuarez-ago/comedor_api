package com.services.comedor.models;

import java.util.Set;

import lombok.Builder;

@Builder 
public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        Double precioBase,
        String imagenUrl,
        Set<GrupoModificadorDTO> gruposModificadores
) {}

