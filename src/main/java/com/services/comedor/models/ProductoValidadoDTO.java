package com.services.comedor.models;

import java.util.List;

/**
 * Detalle de producto validado en caja
 * 
 * @param productoId ID del producto
 * @param nombre Nombre del producto
 * @param cantidad Cantidad solicitada
 * @param modificadores Lista de modificadores (ej: "Sin cebolla")
 * @param controlaStock Indica si descuenta stock (true) o no (false)
 * @param stockRestante Stock restante después del descuento (si aplica)
 */
public record ProductoValidadoDTO(
        Long productoId,
        String nombre,
        Integer cantidad,
        List<String> modificadores,
        Boolean controlaStock,
        Integer stockRestante
) {}