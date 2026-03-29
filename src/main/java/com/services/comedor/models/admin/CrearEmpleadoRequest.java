package com.services.comedor.models.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CrearEmpleadoRequest(
        @NotBlank(message = "Numero de empleado es requerido")
        @Size(min = 3, max = 20, message = "Numero debe tener entre 3 y 20 caracteres")
        String numeroEmpleado,
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,
        @NotBlank(message = "El telefono es requerido")
        @Pattern(regexp = "^[0-9]{10}$", message = "Telefono invalido. Debe tener 10 digitos")
        String telefono,
        @NotBlank(message = "El PIN es requerido")
        @Size(min = 4, max = 8, message = "El PIN debe tener entre 4 y 8 caracteres")
        String pin,
        @NotNull(message = "El comedor es requerido")
        Long comedorId,
        @NotNull(message = "horarioFlexible es requerido")
        Boolean horarioFlexible,
        Set<Long> tiposConsumoPermitidosIds,
        Boolean activo
) {}
