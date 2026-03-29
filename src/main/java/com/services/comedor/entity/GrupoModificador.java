package com.services.comedor.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

/**
 * 🧩 ENTIDAD: GRUPO_MODIFICADORES
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Representa un GRUPO de opciones que el empleado puede elegir al pedir un producto.
 * Ejemplos de grupos:
 *   - "Elige tu salsa" (opciones: salsa roja, salsa verde, salsa de cacahuate)
 *   - "Tipo de tortilla" (opciones: maíz, harina)
 *   - "Extras" (opciones: queso extra, tocino, aguacate)
 * 
 * ================================================================================================================
 * ESTRUCTURA JERÁRQUICA
 * ================================================================================================================
 * 
 * ┌─────────────────┐     ┌─────────────────────┐     ┌─────────────────────┐
 * │    PRODUCTO     │     │  GRUPO_MODIFICADOR  │     │ OPCION_MODIFICADOR  │
 * ├─────────────────┤     ├─────────────────────┤     ├─────────────────────┤
 * │ id (PK)         │────►│ producto_id (FK)    │────►│ grupo_id (FK)       │
 * │ nombre          │     │ nombre (ej: "Salsas")│     │ nombre (ej: "Roja") │
 * │ descripcion     │     │ minimo (ej: 1)      │     │ precio_extra (ej: 0)│
 * └─────────────────┘     │ maximo (ej: 2)      │     └─────────────────────┘
 *                         └─────────────────────┘
 * 
 * ================================================================================================================
 * REGLAS DE SELECCIÓN
 * ================================================================================================================
 * 
 * - minimo: número mínimo de opciones que DEBE elegir el empleado (ej: 1 salsa obligatoria)
 * - maximo: número máximo de opciones que PUEDE elegir (ej: máximo 2 salsas)
 * 
 * Ejemplo con valores:
 *   Grupo: "Elige tu salsa"
 *     minimo = 1 (obligatorio elegir al menos una)
 *     maximo = 2 (puede elegir hasta dos)
 * 
 * ================================================================================================================
 * EJEMPLO DE USO EN APP
 * ================================================================================================================
 * 
 * Empleado selecciona "TACOS DE CANASTA":
 *   └── Grupo: "Elige tu salsa"
 *       ├── [ ] Salsa roja
 *       ├── [x] Salsa verde
 *       └── [ ] Salsa de cacahuate
 *   └── Grupo: "Extras"
 *       ├── [ ] Queso extra (+$10)
 *       └── [x] Cebolla
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "grupos_modificadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoModificador {
    
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
     * 🍲 PRODUCTO al que pertenece este grupo de modificadores
     * Relación Many-to-One: un producto puede tener múltiples grupos
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
    
    // =====================================================
    // DATOS DEL GRUPO
    // =====================================================
    
    /**
     * 🏷️ NOMBRE del grupo de modificadores
     * Ejemplos: "Elige tu salsa", "Tipo de tortilla", "Extras"
     */
    @Column(length = 80)
    private String nombre;
    
    /**
     * 🔢 NÚMERO MÍNIMO de opciones que DEBE elegir el empleado
     * - 0: opcional (no es obligatorio elegir nada)
     * - 1: obligatorio elegir al menos una opción
     * - 2: obligatorio elegir al menos dos opciones
     */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer minimo;
    
    /**
     * 🔢 NÚMERO MÁXIMO de opciones que PUEDE elegir el empleado
     * - 1: solo puede elegir una opción (radio buttons)
     * - 2: puede elegir hasta dos (checkboxes)
     * - 3: puede elegir hasta tres
     */
    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer maximo;
    
    // =====================================================
    // OPCIONES
    // =====================================================
    
    /**
     * 🎯 OPCIONES disponibles dentro de este grupo
     * Ejemplos para "Salsas":
     *   - Salsa roja (precio 0)
     *   - Salsa verde (precio 0)
     *   - Salsa de cacahuate (precio 0)
     * 
     * Relación One-to-Many con OpcionModificador
     */
    @OneToMany(mappedBy = "grupoModificador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OpcionModificador> opciones;
}