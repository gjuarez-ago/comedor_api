package com.services.comedor.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

/**
 * 🎯 ENTIDAD: OPCION_MODIFICADOR
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Representa una OPCIÓN ESPECÍFICA dentro de un grupo de modificadores.
 * Es el nivel más granular de personalización que un empleado puede elegir al pedir un producto.
 * 
 * ================================================================================================================
 * JERARQUÍA COMPLETA
 * ================================================================================================================
 * 
 * PRODUCTO (ej: TACOS DE CANASTA)
 *    │
 *    └── GRUPO_MODIFICADOR (ej: "Elige tu salsa")
 *           │
 *           ├── OPCION_MODIFICADOR (ej: "Salsa roja")
 *           ├── OPCION_MODIFICADOR (ej: "Salsa verde")
 *           └── OPCION_MODIFICADOR (ej: "Salsa de cacahuate")
 * 
 * ================================================================================================================
 * EJEMPLOS DE USO
 * ================================================================================================================
 * 
 * | Grupo                | Opción            | precioExtra | productoVinculado     |
 * |----------------------|-------------------|-------------|-----------------------|
 * | Elige tu salsa       | Salsa roja        | 0.00        | NULL                  |
 * | Elige tu salsa       | Salsa verde       | 0.00        | NULL                  |
 * | Extras               | Queso extra       | 10.00       | NULL                  |
 * | Extras               | Tocino            | 15.00       | NULL                  |
 * | Sustitución          | Tortilla de harina| 0.00        | producto_id_tortilla  |
 * 
 * ================================================================================================================
 * CAMPOS IMPORTANTES
 * ================================================================================================================
 * 
 * - precioExtra: costo adicional por elegir esta opción (ej: +$10 por queso extra)
 * - productoVinculado: cuando la opción sustituye a otro producto (ej: cambiar tortilla)
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "opciones_modificadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionModificador {
    
    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // RELACIONES
    // =====================================================
    
    /**
     * 🧩 GRUPO al que pertenece esta opción
     * Relación Many-to-One: un grupo puede tener muchas opciones
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private GrupoModificador grupoModificador;
    
    // =====================================================
    // DATOS DE LA OPCIÓN
    // =====================================================
    
    /**
     * 🏷️ NOMBRE de la opción
     * Ejemplos: "Salsa roja", "Queso extra", "Sin cebolla", "Tortilla de harina"
     */
    @Column(length = 80)
    private String nombre;
    
    /**
     * 🔗 PRODUCTO VINCULADO (opcional)
     * - Si no es NULL, esta opción sustituye a otro producto
     * - Ejemplo: cambiar tortilla de maíz por tortilla de harina
     * - Útil para menús donde ciertas opciones implican un producto diferente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_vinculado_id")
    private Producto productoVinculado;
    
    /**
     * 💰 PRECIO EXTRA por elegir esta opción
     * - 0.00: sin costo adicional
     * - > 0.00: tiene costo extra (ej: queso extra +$10)
     * - Se aplica al momento de calcular el total del pedido
     */
    private BigDecimal precioExtra;
}