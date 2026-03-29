package com.services.comedor.models;

import lombok.Builder;

@Builder
public record ModificadorDTO(Long id, String nombre, Double precioExtra) {}

