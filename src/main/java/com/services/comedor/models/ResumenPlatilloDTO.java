package com.services.comedor.models;

import java.util.Set;

public record ResumenPlatilloDTO(String nombre, Integer cantidad, Set<String> notas) {}

