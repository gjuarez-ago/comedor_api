package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 🧾 ENTIDAD: CONSUMO_DETALLE
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Esta tabla registra CADA PRODUCTO incluido en un consumo.
 * Un consumo puede tener múltiples detalles (ej: una comida + un snack).
 * 
 * ================================================================================================================
 * EJEMPLO DE USO
 * ================================================================================================================
 * 
 * Consumo #100:
 *   ├── Detalle 1: COMIDA CORRIENTE x1, precio $0.00 (empleado) / $55.00 (empresa)
 *   └── Detalle 2: GALLETA x2, precio $5.00 (empleado) / $5.00 (empresa) cada una
 * 
 * ================================================================================================================
 * RELACIONES
 * ================================================================================================================
 * 
 * ┌─────────────┐     ┌──────────────────┐     ┌─────────────────────────┐
 * │  CONSUMO    │     │ CONSUMO_DETALLE  │     │      PRODUCTO           │
 * ├─────────────┤     ├──────────────────┤     ├─────────────────────────┤
 * │ id (PK)     │────►│ consumo_id (FK)  │     │ id (PK)                 │
 * │ ...         │     │ producto_id (FK) │────►│ nombre                  │
 * └─────────────┘     │ cantidad         │     │ precio_empleado (base)  │
 *                     │ precio_empleado  │     └─────────────────────────┘
 *                     │ precio_empresa   │
 *                     └──────────────────┘
 *                            │
 *                            │ (OneToMany)
 *                            ▼
 *              ┌─────────────────────────────────┐
 *              │ CONSUMO_DETALLE_MODIFICADORES   │
 *              ├─────────────────────────────────┤
 *              │ id                              │
 *              │ detalle_id (FK)                 │
 *              │ nombre_opcion (ej: "Sin cebolla")│
 *              │ precio_extra                    │
 *              └─────────────────────────────────┘
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "consumo_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumoDetalle {
    
    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // RELACIONES PRINCIPALES
    // =====================================================
    
    /**
     * 🧾 CONSUMO al que pertenece este detalle
     * Relación Many-to-One: un consumo puede tener muchos detalles
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_id", nullable = false)
    private Consumo consumo;
    
    /**
     * 🍲 PRODUCTO que se está consumiendo
     * Ejemplo: COMIDA CORRIENTE, GALLETA EMPERADOR, etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    // =====================================================
    // CANTIDAD
    // =====================================================
    
    /**
     * 📦 CANTIDAD de este producto en el pedido
     * Ejemplo: 2 galletas, 1 comida, etc.
     */
    @Column(nullable = false)
    private Integer cantidad;
    
    // =====================================================
    // PRECIOS (CONGELADOS EN EL MOMENTO DEL PEDIDO)
    // =====================================================
    
    /**
     * 💰 PRECIO UNITARIO para el EMPLEADO en el momento del pedido
     * - Se congela al crear el pedido (no cambia aunque se modifique el precio después)
     * - Snacks tienen precio real, comidas generalmente 0.00
     * - Útil para reportes históricos
     */
    @Column(name = "precio_unitario_empleado", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioEmpleado;
    
    /**
     * 🏢 PRECIO UNITARIO para la EMPRESA en el momento del pedido
     * - Costo real que la empresa paga por este producto
     * - Se congela al crear el pedido
     * - Útil para facturación interna y reportes financieros
     */
    @Column(name = "precio_unitario_empresa", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioEmpresa;
    
    // =====================================================
    // MODIFICADORES (opciones extras)
    // =====================================================
    
    /**
     * 📝 MODIFICADORES aplicados a este producto
     * Ejemplos: "Sin cebolla", "Extra queso (+$10)", "Bien cocido"
     * Relación One-to-Many con ConsumoDetalleModificador
     */
    @OneToMany(mappedBy = "consumoDetalle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsumoDetalleModificador> modificadores;
    
    // =====================================================
    // MÉTODOS DE CÁLCULO
    // =====================================================
    
    /**
     * 🧮 Calcula el subtotal para el EMPLEADO
     * Subtotal = precio_unitario_empleado × cantidad
     * 
     * @return BigDecimal con el subtotal
     */
    public BigDecimal getSubtotalEmpleado() {
        if (precioUnitarioEmpleado == null || cantidad == null) return BigDecimal.ZERO;
        return precioUnitarioEmpleado.multiply(BigDecimal.valueOf(cantidad));
    }
    
    /**
     * 🧮 Calcula el subtotal para la EMPRESA
     * Subtotal = precio_unitario_empresa × cantidad
     * 
     * @return BigDecimal con el subtotal
     */
    public BigDecimal getSubtotalEmpresa() {
        if (precioUnitarioEmpresa == null || cantidad == null) return BigDecimal.ZERO;
        return precioUnitarioEmpresa.multiply(BigDecimal.valueOf(cantidad));
    }
}