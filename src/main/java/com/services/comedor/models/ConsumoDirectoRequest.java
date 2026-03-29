package com.services.comedor.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request para venta directa (empleado sin celular)
 * 
 * @param numeroEmpleado Número de nómina/gafete del empleado
 * @param comedorId ID del comedor donde se realiza la venta (auditoría)
 * @param detalles Lista de productos seleccionados
 */
public record ConsumoDirectoRequest(
    
    @NotBlank(message = "Número de empleado requerido")
    String numeroEmpleado,
    
    @NotNull(message = "Comedor es requerido para auditoría")
    Long comedorId,
    
    @NotEmpty(message = "Debe seleccionar al menos un producto")
    List<DetalleRequest> detalles
    
) {}