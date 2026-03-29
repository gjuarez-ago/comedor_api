package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 📦 ENTIDAD: PRODUCTO_STOCK
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Controla el STOCK ACTUAL de productos que tienen inventario limitado.
 * Cada registro representa el stock de un producto en un comedor específico.
 * 
 * ================================================================================================================
 * ¿QUÉ PRODUCTOS TIENEN STOCK?
 * ================================================================================================================
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 * │  SNACKS                              │  COMIDAS CON PORCIONES LIMITADAS                                    │
 * ├───────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤
 * │  controla_inventario = true           │  controla_porciones = true                                          │
 * │  Ej: Galletas, Refrescos, Agua       │  Ej: COMIDA CORRIENTE (solo 50 porciones/día)                      │
 * │  Stock independiente por comedor     │  Stock independiente por comedor                                    │
 * └─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ================================================================================================================
 * EJEMPLO DE DATOS
 * ================================================================================================================
 * 
 * | id | producto_id | comedor_id | stock_actual |
 * |----|-------------|------------|--------------|
 * | 1  | 13 (Galleta)| 1 (Norte)  | 50           |
 * | 2  | 13 (Galleta)| 2 (Sur)    | 45           |
 * | 3  | 15 (Refresco)| 1 (Norte) | 100          |
 * | 4  | 4 (Comida)  | 1 (Norte)  | 49           |
 * 
 * ================================================================================================================
 * OPERACIONES ATÓMICAS (sin race conditions)
 * ================================================================================================================
 * 
 * Para evitar que dos personas compren el último producto al mismo tiempo:
 * 
 * UPDATE producto_stock 
 * SET stock_actual = stock_actual - 1
 * WHERE producto_id = 13 AND comedor_id = 1 AND stock_actual >= 1
 * 
 * La consulta retorna el número de filas afectadas (1 si éxito, 0 si sin stock)
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(
        name = "producto_stock",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"producto_id", "comedor_id"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoStock {
    
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
     * 🍲 PRODUCTO al que pertenece este stock
     * Relación Many-to-One: un producto puede tener stock en múltiples comedores
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    /**
     * 🏭 COMEDOR donde está ubicado este stock
     * Relación Many-to-One: un comedor puede tener stock de múltiples productos
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;
    
    // =====================================================
    // STOCK
    // =====================================================
    
    /**
     * 🔢 STOCK ACTUAL disponible
     * - Para snacks: unidades físicas disponibles
     * - Para comidas: porciones disponibles para el día
     * - Valor por defecto: 0
     */
    @Column(name = "stock_actual", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stockActual;
}