package com.services.comedor.models;

import java.util.List;

import com.services.comedor.enums.EstadoConsumo;

/**
 * DTO para una fila en el panel de despacho
 * 
 * @param pedidoId ID del pedido
 * @param folio Folio del pedido (ej: "NORTE-001")
 * @param nombreEmpleado Nombre del empleado que pidió
 * @param numeroEmpleado Número del empleado
 * @param estado Estado actual (LISTO, PREPARANDO, etc.)
 * @param items Lista de productos con sus modificadores
 * @param tiempoEspera Tiempo transcurrido desde creación
 */
public record FilaDespachoDTO(
        Long pedidoId,
        String folio,
        String nombreEmpleado,
        String numeroEmpleado,
        EstadoConsumo estado,
        List<ResumenPlatilloDTO> items,
        String tiempoEspera
) {}