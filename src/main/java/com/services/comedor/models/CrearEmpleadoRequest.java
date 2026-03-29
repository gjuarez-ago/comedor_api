package com.services.comedor.models;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearEmpleadoRequest(
    
    @NotBlank(message = "El número de empleado es requerido")
    @Size(min = 3, max = 20, message = "El número debe tener entre 3 y 20 caracteres")
    String numeroEmpleado,
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,
    
    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "^[0-9]{10}$", message = "Teléfono inválido. Debe tener 10 dígitos")
    String telefono,
    
    @NotBlank(message = "El PIN es requerido")
    @Size(min = 4, max = 8, message = "El PIN debe tener entre 4 y 8 caracteres")
    String pin,
    
    @NotNull(message = "El comedor es requerido")
    Long comedorId,
    
    @NotNull(message = "Debe especificar si es horario flexible (camionero) o no (administrativo)")
    Boolean horarioFlexible,
    
    @NotEmpty(message = "Debe seleccionar al menos un tipo de consumo permitido")
    Set<Long> tiposConsumoPermitidosIds,
    
    Boolean activo
) {}