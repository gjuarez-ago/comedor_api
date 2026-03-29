package com.services.comedor.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

/**
 * 🍲 ENTIDAD: PRODUCTO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Catálogo maestro de TODOS los productos que se pueden vender en el comedor.
 * Cada producto puede ser una COMIDA (requiere preparación) o un SNACK (venta directa).
 * 
 * ================================================================================================================
 * TIPOS DE PRODUCTOS
 * ================================================================================================================
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 * │  COMIDAS                              │  SNACKS                                                            │
 * ├───────────────────────────────────────┼───────────────────────────────────────────────────────────────────┤
 * │  requiere_preparacion = true          │  requiere_preparacion = false                                     │
 * │  controla_inventario = false          │  controla_inventario = true                                        │
 * │  controla_porciones = true/false      │  controla_porciones = false                                        │
 * │  precio_empleado = 0 (beneficio)      │  precio_empleado = precio real                                    │
 * │  Ej: COMIDA CORRIENTE, MOLE, CARNITAS │  Ej: GALLETA, REFRESCO, AGUA, CAFÉ                                │
 * └─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ================================================================================================================
 * CONTROL DE STOCK
 * ================================================================================================================
 * 
 * | Campo               | Snacks          | Comidas sin límite | Comidas con porciones |
 * |---------------------|-----------------|--------------------|-----------------------|
 * | controla_inventario | true            | false              | false                 |
 * | controla_porciones  | false           | false              | true                  |
 * | stock en BD         | producto_stock  | -                  | producto_stock        |
 * 
 * ================================================================================================================
 * EJEMPLOS DE DATOS
 * ================================================================================================================
 * 
 * | id | nombre             | requiere_prep | controla_inv | controla_porc | precio_empleado |
 * |----|--------------------|---------------|--------------|---------------|-----------------|
 * | 4  | COMIDA CORRIENTE   | true          | false        | true          | 0.00            |
 * | 5  | MOLE POBLANO       | true          | false        | true          | 0.00            |
 * | 13 | GALLETA EMPERADOR  | false         | true         | false         | 5.00            |
 * | 15 | REFRESCO COCA-COLA | false         | true         | false         | 12.00           |
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {
    
    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // DATOS BÁSICOS
    // =====================================================
    
    /**
     * 🏷️ NOMBRE del producto
     * Ejemplos: "COMIDA CORRIENTE", "GALLETA EMPERADOR", "REFRESCO COCA-COLA"
     */
    @Column(nullable = false, length = 120)
    private String nombre;
    
    /**
     * 📝 DESCRIPCIÓN del producto para mostrar en la app
     * Ejemplo: "Arroz, frijoles, carne asada, ensalada"
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    /**
     * 🖼️ URL de la imagen del producto (para mostrar en la app)
     * Puede ser de CDN, S3, o almacenamiento local
     */
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;
    
    // =====================================================
    // COMPORTAMIENTO
    // =====================================================
    
    /**
     * 🍳 INDICA si el producto REQUIERE PREPARACIÓN EN COCINA
     * - true: el producto pasa por cocina (comidas)
     * - false: el producto se entrega directamente (snacks)
     */
    @Column(name = "requiere_preparacion", columnDefinition = "boolean default false")
    private Boolean requierePreparacion;
    
    /**
     * 📦 INDICA si el producto CONTROLA INVENTARIO (snacks)
     * - true: tiene stock en tabla producto_stock
     * - false: no controla stock (comidas sin límite)
     */
    @Column(name = "controla_inventario", columnDefinition = "boolean default false")
    private Boolean controlaInventario;
    
    /**
     * 🥩 INDICA si el producto CONTROLA PORCIONES (comidas con límite)
     * - true: tiene stock limitado (ej: solo 50 porciones de COMIDA CORRIENTE)
     * - false: sin límite de porciones
     */
    @Column(name = "controla_porciones", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean controlaPorciones = false;
    
    // =====================================================
    // ESTADO
    // =====================================================
    
    /**
     * 🟢 INDICA si el producto está ACTIVO
     * - true: visible en el menú
     * - false: oculto (no aparece en la app)
     */
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean activo = true;
    
    /**
     * 📊 CÓDIGO DE BARRAS (opcional)
     * Para integración con lectores de códigos de barras en caja
     */
    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;
    
    // =====================================================
    // RELACIONES
    // =====================================================
    
    /**
     * 🧩 GRUPOS DE MODIFICADORES asociados a este producto
     * Ejemplo: para TACOS, puede tener grupo "Elige tu salsa"
     */
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private Set<GrupoModificador> gruposModificadores;
}