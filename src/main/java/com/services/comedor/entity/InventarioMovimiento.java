package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📦 ENTIDAD: INVENTARIO_MOVIMIENTOS
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Registra CADA cambio en el inventario de productos que controlan stock.
 * Funciona como un AUDITORÍA DE INVENTARIO, permitiendo rastrear:
 *   - Cuándo se descontó stock (por consumo)
 *   - Cuándo se devolvió stock (por cancelación)
 *   - Cuándo se ajustó stock manualmente (por administrador)
 * 
 * ================================================================================================================
 * TIPOS DE MOVIMIENTOS
 * ================================================================================================================
 * 
 * | tipo    | significado                          | cantidad | motivo común      |
 * |---------|--------------------------------------|----------|-------------------|
 * | SALIDA  | Se vendió/consumió producto          | positiva | "CONSUMO"         |
 * | ENTRADA | Se devolvió o agregó stock           | positiva | "CANCELACION"     |
 * | AJUSTE  | Corrección manual de inventario      | +/-      | "AJUSTE_MANUAL"   |
 * | COMPRA  | Llegó mercancía nueva                | positiva | "COMPRA_PROVEEDOR"|
 * 
 * ================================================================================================================
 * EJEMPLO DE USO
 * ================================================================================================================
 * 
 * Consumo #100 (Galleta):
 *   ├── Se descuenta 1 unidad de stock
 *   └── Se registra movimiento:
 *       | tipo="SALIDA" | cantidad=1 | motivo="CONSUMO" | consumo_id=100
 * 
 * Cancelación del consumo #100:
 *   ├── Se devuelve 1 unidad de stock
 *   └── Se registra movimiento:
 *       | tipo="ENTRADA" | cantidad=1 | motivo="CANCELACION" | consumo_id=100
 * 
 * ================================================================================================================
 * RELACIONES
 * ================================================================================================================
 * 
 * ┌─────────────────┐     ┌─────────────────────────┐     ┌─────────────────┐
 * │    PRODUCTO     │     │ INVENTARIO_MOVIMIENTO   │     │    CONSUMO      │
 * ├─────────────────┤     ├─────────────────────────┤     ├─────────────────┤
 * │ id (PK)         │────►│ producto_id (FK)        │     │ id (PK)         │
 * │ nombre          │     │ consumo_id (FK)         │────►│ ...             │
 * │ controla_stock  │     │ tipo (SALIDA/ENTRADA)   │     └─────────────────┘
 * └─────────────────┘     │ cantidad                │
 *                         │ fecha                   │
 *                         │ motivo                  │
 *                         └─────────────────────────┘
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "inventario_movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioMovimiento {
    
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
     * 🍲 PRODUCTO afectado por este movimiento
     * Relación Many-to-One: un producto puede tener muchos movimientos
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
    
    /**
     * 🧾 CONSUMO asociado (si aplica)
     * - Si el movimiento es por consumo/cancelación, referencia al consumo
     * - Si es ajuste manual, puede ser NULL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_id")
    private Consumo consumo;
    
    // =====================================================
    // DATOS DEL MOVIMIENTO
    // =====================================================
    
    /**
     * 🏷️ TIPO DE MOVIMIENTO
     * Valores posibles: "SALIDA", "ENTRADA", "AJUSTE", "COMPRA"
     */
    @Column(length = 10)
    private String tipo;
    
    /**
     * 🔢 CANTIDAD movida
     * - Siempre positiva (el tipo indica si es entrada o salida)
     * - Ejemplo: 1, 5, 10
     */
    @Column(nullable = false)
    private Integer cantidad;
    
    /**
     * ⏰ FECHA Y HORA del movimiento
     * Se asigna automáticamente al persistir
     */
    @Column(updatable = false)
    private LocalDateTime fecha;
    
    /**
     * 📝 MOTIVO del movimiento
     * Ejemplos: "CONSUMO", "CANCELACION", "AJUSTE_MANUAL", "COMPRA_PROVEEDOR"
     */
    @Column(length = 100)
    private String motivo;
    
    // =====================================================
    // LÓGICA DE INICIALIZACIÓN
    // =====================================================
    
    /**
     * 🔧 Se ejecuta ANTES de guardar el registro por primera vez
     * Asigna automáticamente la fecha y hora actual
     */
    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}