package com.services.comedor.models.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearComedorRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,
        Boolean activo
) {}

