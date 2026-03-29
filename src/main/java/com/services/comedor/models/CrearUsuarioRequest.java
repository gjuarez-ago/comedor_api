package com.services.comedor.models;

import jakarta.validation.constraints.*;
import java.util.List;

public record CrearUsuarioRequest(
    
    @NotBlank(message = "El username es requerido")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    String username,
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,
    
    @NotBlank(message = "El PIN es requerido")
    @Size(min = 4, max = 8, message = "El PIN debe tener entre 4 y 8 caracteres")
    String pin,
    
    @NotBlank(message = "El rol es requerido")
    @Pattern(regexp = "ROLE_ADMIN|ROLE_CAJERO|ROLE_COCINA|ROLE_JEFE_COMEDOR", 
             message = "Rol inválido. Roles permitidos: ROLE_ADMIN, ROLE_CAJERO, ROLE_COCINA, ROLE_JEFE_COMEDOR")
    String rol,
    
    @NotNull(message = "El comedor base es requerido")
    Long comedorBaseId,
    
    @NotEmpty(message = "Debe seleccionar al menos un comedor permitido")
    List<Long> comedoresPermitidosIds,
    
    Boolean activo
) {}