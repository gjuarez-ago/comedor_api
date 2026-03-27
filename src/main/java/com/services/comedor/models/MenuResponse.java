package com.services.comedor.models;

import java.util.List;

public record MenuResponse(String turnoNombre, String horario, List<ProductoDTO> productos) {}

