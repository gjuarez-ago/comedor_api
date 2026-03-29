package com.services.comedor.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CrearConsumoRequest(
    @NotNull(message = "El tipo de consumo es requerido")
    Long tipoConsumoId,
    
    @NotEmpty(message = "Debe seleccionar al menos un producto")
    List<DetalleRequest> detalles
) {}