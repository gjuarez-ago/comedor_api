package com.services.comedor.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

/**
 * 📦 ENTIDAD: COMEDOR_PRODUCTO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Esta tabla es el CORAZÓN de la configuración del menú. Define cómo se comporta un producto específico
 * dentro de un comedor concreto. Permite:
 * 
 * 1. Precios diferenciados por comedor (un producto puede tener precio distinto en cada planta)
 * 2. Disponibilidad granular (activar/desactivar un producto en un comedor específico)
 * 3. Asignación de tipos de consumo (definir si el producto se sirve en desayuno, comida, cena o tienda)
 * 
 * ================================================================================================================
 * EJEMPLO DE USO
 * ================================================================================================================
 * 
 * COMEDOR NORTE (id=1) con COMIDA CORRIENTE (producto_id=4):
 *   - Precio para empleado: $0.00 (beneficio)
 *   - Precio para empresa: $55.00
 *   - Disponible: true
 *   - Se sirve en: COMIDA (tipo_consumo_id=2) y puede ser también SNACK si aplica
 * 
 * COMEDOR SUR (id=2) con la misma COMIDA CORRIENTE:
 *   - Puede tener precio distinto: $0.00 empleado, $58.00 empresa
 *   - O incluso NO estar disponible (disponible=false)
 * 
 * ================================================================================================================
 * RELACIONES
 * ================================================================================================================
 * 
 * ┌──────────────────┐     ┌──────────────────────┐     ┌──────────────────┐
 * │    COMEDOR       │     │   COMEDOR_PRODUCTO   │     │    PRODUCTO      │
 * ├──────────────────┤     ├──────────────────────┤     ├──────────────────┤
 * │ id (PK)          │────►│ comedor_id (FK)      │◄────│ id (PK)          │
 * │ nombre           │     │ producto_id (FK)     │     │ nombre           │
 * │ activo           │     │ precio_empleado      │     │ descripcion      │
 * └──────────────────┘     │ precio_empresa       │     │ imagen_url       │
 *                          │ disponible           │     │ requiere_prep    │
 *                          └──────────────────────┘     │ controla_stock   │
 *                                 │                     └──────────────────┘
 *                                 │ (ManyToMany)
 *                                 ▼
 *                    ┌─────────────────────────────────┐
 *                    │      TIPOS_CONSUMO              │
 *                    ├─────────────────────────────────┤
 *                    │ id (PK)                         │
 *                    │ nombre (DESAYUNO/COMIDA/CENA)   │
 *                    └─────────────────────────────────┘
 * 
 * ================================================================================================================
 * RESTRICCIONES
 * ================================================================================================================
 * 
 * - Un mismo producto NO puede estar duplicado en el mismo comedor (UniqueConstraint)
 * - Si un producto no tiene tipos asignados en turnosDisponibles, NO aparecerá en ningún menú
 * - precio_empleado = 0 generalmente indica beneficio para el empleado (comida gratuita)
 * - precio_empresa es el costo real que la empresa paga por ese producto
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(
        name = "comedor_productos",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"comedor_id", "producto_id"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComedorProducto {
    
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
     * 🏭 COMEDOR al que pertenece esta configuración.
     * Relación Many-to-One: un comedor puede tener muchos productos configurados.
     * Ejemplo: Comedor Norte, Comedor Sur, etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;
    
    /**
     * 🍲 PRODUCTO que se está configurando.
     * Relación Many-to-One: un producto puede estar configurado en múltiples comedores.
     * Ejemplo: COMIDA CORRIENTE, MOLE POBLANO, GALLETA, etc.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    // =====================================================
    // PRECIOS (diferenciados por comedor)
    // =====================================================
    
    /**
     * 💰 PRECIO QUE PAGA EL EMPLEADO.
     * - Normalmente 0.00 para comidas (beneficio para el empleado)
     * - Snacks tienen precio real (ej: 5.00 para galleta)
     * - Puede variar por comedor si es necesario
     * - Valor por defecto: 0.00
     */
    @Column(
            name = "precio_empleado",
            precision = 10,
            scale = 2,
            columnDefinition = "DECIMAL(10,2) DEFAULT 0.00"
    )
    @Builder.Default
    private BigDecimal precioEmpleado = BigDecimal.ZERO;
    
    /**
     * 🏢 PRECIO QUE PAGA LA EMPRESA (costo real).
     * - Representa el costo real que la empresa asume por cada producto
     * - Útil para reportes financieros y facturación interna
     * - Generalmente no varía por comedor, pero se mantiene por flexibilidad
     * - Valor por defecto: 0.00
     */
    @Column(
            name = "precio_empresa",
            precision = 10,
            scale = 2,
            columnDefinition = "DECIMAL(10,2) DEFAULT 0.00"
    )
    @Builder.Default
    private BigDecimal precioEmpresa = BigDecimal.ZERO;
    
    // =====================================================
    // DISPONIBILIDAD
    // =====================================================
    
    /**
     * 🔘 INDICADOR DE DISPONIBILIDAD del producto en este comedor.
     * - true: el producto está disponible para pedir
     * - false: el producto NO se muestra en el menú de este comedor
     * - Útil para desactivar productos temporalmente sin eliminar la configuración
     * - Valor por defecto: true
     */
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean disponible = true;
    
    // =====================================================
    // TIPOS DE CONSUMO (cuándo se puede pedir)
    // =====================================================
    
    /**
     * 📅 TIPOS DE CONSUMO donde este producto está disponible.
     * 
     * Define EN QUÉ MOMENTOS se puede pedir este producto:
     * - DESAYUNO (id=1): solo en horario de desayuno
     * - COMIDA (id=2): solo en horario de comida
     * - CENA (id=3): solo en horario de cena
     * - TIENDA (id=99): siempre disponible (snacks)
     * 
     * Un producto puede estar en MÚLTIPLES tipos:
     * - Ejemplo: Ensalada puede estar en COMIDA y también en CENA
     * - Ejemplo: Galleta solo en TIENDA
     * - Ejemplo: Desayuno completo solo en DESAYUNO
     * 
     * Si un producto NO tiene tipos asignados, NO aparecerá en ningún menú.
     * 
     * Relación Many-to-Many con la tabla tipos_consumo.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "comedor_producto_turnos",
        joinColumns = @JoinColumn(name = "comedor_producto_id"),
        inverseJoinColumns = @JoinColumn(name = "tipo_consumo_id")
    )
    @Builder.Default
    private Set<TipoConsumo> turnosDisponibles = new HashSet<>();
}