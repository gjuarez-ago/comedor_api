package com.services.comedor.models;

import java.util.Set;

public record DetalleKdsDTO(String productoNombre, Integer cantidad, Set<ModificadorKdsDTO> notas) {}

