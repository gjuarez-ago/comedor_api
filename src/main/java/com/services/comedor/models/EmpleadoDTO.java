package com.services.comedor.models;

/**
 * DTO para respuesta de búsqueda de empleados
 * 
 * @param id ID interno del empleado
 * @param numeroEmpleado Número de nómina/gafete (ej: "CAM001")
 * @param nombre Nombre completo del empleado
 * @param telefono Teléfono de contacto
 * @param comedorNombre Nombre del comedor base
 * @param horarioFlexible true = camionero, false = administrativo
 */
public record EmpleadoDTO(
        Long id,
        String numeroEmpleado,
        String nombre,
        String telefono,
        String comedorNombre,
        Boolean horarioFlexible
) {}