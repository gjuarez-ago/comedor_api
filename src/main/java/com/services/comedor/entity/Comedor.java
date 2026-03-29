package com.services.comedor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📍 ENTIDAD: COMEDOR
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Representa una planta física o ubicación donde los empleados pueden consumir sus alimentos.
 * Cada comedor tiene su propia configuración de horarios, productos disponibles y stock.
 * 
 * ================================================================================================================
 * USO EN EL SISTEMA
 * ================================================================================================================
 * 
 * 1. Cada empleado está asignado a un comedor base (donde normalmente come)
 * 2. Cada usuario del sistema (cajero/cocina) puede operar en uno o múltiples comedores
 * 3. Los horarios de servicio son específicos por comedor
 * 4. Los productos tienen precios y disponibilidad por comedor
 * 5. El stock de snacks es independiente por comedor
 * 
 * ================================================================================================================
 * EJEMPLO DE DATOS
 * ================================================================================================================
 * 
 * | id | nombre         | activo |
 * |----|----------------|--------|
 * | 1  | COMEDOR NORTE  | true   |
 * | 2  | COMEDOR SUR    | true   |
 * | 3  | COMEDOR CENTRAL| true   |
 * | 4  | COMEDOR PLANTA1| true   |
 * | 5  | COMEDOR PLANTA2| true   |
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "comedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comedor {
    
    /**
     * Identificador único del comedor
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del comedor
     * Ejemplos: "COMEDOR NORTE", "COMEDOR SUR", "COMEDOR PLANTA 1"
     */
    @Column(length = 120)
    private String nombre;

    /**
     * Indica si el comedor está operativo
     * - true: el comedor está activo y puede recibir pedidos
     * - false: el comedor está deshabilitado (mantenimiento, cierre temporal)
     * Valor por defecto: true
     */
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean activo = true;
}