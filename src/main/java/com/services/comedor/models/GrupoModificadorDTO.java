package com.services.comedor.models;

import java.util.Set;

import lombok.Builder;

@Builder
public record GrupoModificadorDTO(
        Long id,
        String nombre,
        Integer minSeleccion,
        Integer maxSeleccion,
        Set<ModificadorDTO> opciones
) {}

