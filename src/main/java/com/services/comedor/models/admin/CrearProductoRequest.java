package com.services.comedor.models.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearProductoRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,
        String descripcion,
        String imagenUrl,
        @NotNull(message = "requiere_preparacion es requerido")
        Boolean requierePreparacion,
        Boolean controlaInventario,
        Boolean controlaPorciones,
        Boolean activo,
        List<ConfiguracionComedorDTO> configuracionComedores
) {}

