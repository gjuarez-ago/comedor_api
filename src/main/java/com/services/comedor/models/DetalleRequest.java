package com.services.comedor.models;

import java.util.Set;

public record DetalleRequest(Long productoId, Integer cantidad, Set<Long> modificadoresIds) {}

