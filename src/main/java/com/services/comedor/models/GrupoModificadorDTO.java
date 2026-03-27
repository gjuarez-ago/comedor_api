package com.services.comedor.models;

import java.util.Set;

public record GrupoModificadorDTO(
        Long id,
        String nombre,
        Integer minSeleccion,
        Integer maxSeleccion,
        Set<ModificadorDTO> opciones
) {}

