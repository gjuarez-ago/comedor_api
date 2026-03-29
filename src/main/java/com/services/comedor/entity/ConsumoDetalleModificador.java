package com.services.comedor.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

/**
 * ✏️ ENTIDAD: CONSUMO_DETALLE_MODIFICADORES
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Esta tabla registra las OPCIONES o MODIFICADORES que el empleado eligió para un producto específico.
 * Permite personalizar cada pedido con instrucciones especiales para la cocina.
 * 
 * ================================================================================================================
 * EJEMPLO DE USO
 * ================================================================================================================
 * 
 * Un empleado pide COMIDA CORRIENTE con:
 *   - "Sin cebolla"
 *   - "Extra queso" (costo adicional +$10)
 * 
 * Se crean 2 registros en esta tabla vinculados al mismo ConsumoDetalle.
 * 
 * ================================================================================================================
 * RELACIONES
 * ================================================================================================================
 * 
 * ┌─────────────────────────────────┐     ┌─────────────────────────────────────────┐
 * │       CONSUMO_DETALLE           │     │  CONSUMO_DETALLE_MODIFICADORES         │
 * ├─────────────────────────────────┤     ├─────────────────────────────────────────┤
 * │ id (PK)                         │────►│ detalle_id (FK)                         │
 * │ producto_id                     │     │ nombre_opcion (ej: "Sin cebolla")       │
 * │ cantidad                        │     │ precio_extra (ej: 10.00)                │
 * │ precio_unitario_empleado        │     └─────────────────────────────────────────┘
 * └─────────────────────────────────┘
 * 
 * ================================================================================================================
 * USO EN COCINA (KDS)
 * ================================================================================================================
 * 
 * Cuando el cocinero ve el pedido en la pantalla, se muestra:
 * 
 *   🍲 COMIDA CORRIENTE x1
 *      📝 Notas:
 *         • Sin cebolla
 *         • Extra queso (+$10)
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "consumo_detalle_modificadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumoDetalleModificador {

    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // RELACIÓN CON EL DETALLE
    // =====================================================
    
    /**
     * 🔗 DETALLE al que pertenece este modificador
     * Relación Many-to-One: un detalle puede tener múltiples modificadores
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_id", nullable = false)
    private ConsumoDetalle consumoDetalle;
    
    // =====================================================
    // DATOS DEL MODIFICADOR
    // =====================================================
    
    /**
     * 📝 NOMBRE del modificador
     * Ejemplos: "Sin cebolla", "Extra queso", "Bien cocido", "Sin picante"
     * Se guarda como texto para mantener el valor histórico (no depende del catálogo)
     */
    @Column(name = "nombre_opcion", nullable = false, length = 100)
    private String nombreOpcion;
    
    /**
     * 💰 PRECIO EXTRA del modificador
     * - 0.00 si no tiene costo adicional
     * - > 0.00 si cuesta más (ej: "Extra queso" +$10.00)
     * - Se guarda con el modificador para mantener el precio histórico
     */
    private BigDecimal precioExtra;
    
    // =====================================================
    // MÉTODOS DE IGUALDAD (para Set/HashSet)
    // =====================================================
    
    /**
     * ⚖️ EQUALS: compara por ID (para funcionar correctamente en Sets)
     * Previene duplicados en la colección de modificadores
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsumoDetalleModificador)) return false;
        ConsumoDetalleModificador that = (ConsumoDetalleModificador) o;
        return id != null && id.equals(that.id);
    }
    
    /**
     * 🔢 HASHCODE: basado en la clase (para funcionar con Sets)
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}