package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 🍽️ ENTIDAD: TIPO_CONSUMO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Catálogo que define los DIFERENTES TIPOS DE SERVICIO que ofrece el comedor.
 * Esta tabla es la base para:
 *   - Definir horarios de servicio
 *   - Controlar límites de consumo (ej: 1 desayuno, 1 comida, 1 cena por día)
 *   - Asignar permisos a empleados
 *   - Clasificar productos
 * 
 * ================================================================================================================
 * VALORES FIJOS DEL SISTEMA (NO MODIFICAR)
 * ================================================================================================================
 * 
 * | ID  | NOMBRE     | Descripción                          | Horario | Stock |
 * |-----|------------|--------------------------------------|---------|-------|
 * | 1   | DESAYUNO   | Servicio matutino                    | ✅ Sí   | ❌ No |
 * | 2   | COMIDA     | Servicio principal (almuerzo)        | ✅ Sí   | ❌ No |
 * | 3   | CENA       | Servicio vespertino/nocturno         | ✅ Sí   | ❌ No |
 * | 99  | TIENDA     | Snacks y bebidas (sin preparación)   | ❌ No   | ✅ Sí |
 * 
 * ================================================================================================================
 * USO EN EL SISTEMA
 * ================================================================================================================
 * 
 * 1. HORARIOS: cada tipo puede tener horarios específicos por comedor
 * 2. PERMISOS: cada empleado tiene asignados los tipos que puede consumir
 * 3. PRODUCTOS: cada producto está asociado a los tipos donde se puede pedir
 * 4. CONSUMOS: cada consumo registra el tipo para auditoría
 * 
 * ================================================================================================================
 * EJEMPLO DE DATOS
 * ================================================================================================================
 * 
 * INSERT INTO tipos_consumo (id, nombre) VALUES
 * (1, 'DESAYUNO'),
 * (2, 'COMIDA'),
 * (3, 'CENA'),
 * (99, 'TIENDA');
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "tipos_consumo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoConsumo {
    
    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // NOMBRE DEL TIPO
    // =====================================================
    
    /**
     * 🏷️ NOMBRE del tipo de consumo
     * Valores predefinidos:
     *   - "DESAYUNO"  (ID 1)
     *   - "COMIDA"    (ID 2)
     *   - "CENA"      (ID 3)
     *   - "TIENDA"    (ID 99)
     */
    @Column(nullable = false, length = 50)
    private String nombre;
}