package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * 🔢 ENTIDAD: REGISTRO_FOLIOS
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Mantiene un CONTADOR SECUENCIAL ÚNICO por comedor y por día para generar folios de pedidos.
 * Esta tabla permite generar números consecutivos sin conflictos de concurrencia.
 * 
 * ================================================================================================================
 * ¿POR QUÉ ES NECESARIA?
 * ================================================================================================================
 * 
 * Los pedidos necesitan un FOLIO LEGIBLE y ÚNICO para que los empleados lo identifiquen.
 * Formato: [COMEDOR]-[YYMMDD]-[NÚMERO SECUENCIAL]
 * Ejemplo: NORTE-260327-0045
 *          │      │        └── 45° pedido del día
 *          │      └── 27 de marzo de 2026
 *          └── Comedor Norte
 * 
 * ================================================================================================================
 * EJEMPLO DE DATOS
 * ================================================================================================================
 * 
 * | id | canal   | fecha       | ultimo_folio | version |
 * |----|---------|-------------|--------------|---------|
 * | 1  | NORTE   | 2026-03-27  | 45           | 1       |
 * | 2  | NORTE   | 2026-03-28  | 12           | 1       |
 * | 3  | SUR     | 2026-03-28  | 8            | 1       |
 * 
 * ================================================================================================================
 * GENERACIÓN ATÓMICA (sin race conditions)
 * ================================================================================================================
 * 
 * Usando INSERT ... ON CONFLICT (PostgreSQL/MySQL):
 * 
 * INSERT INTO registro_folios (canal, fecha, ultimo_folio)
 * VALUES ('NORTE', '2026-03-28', 1)
 * ON CONFLICT (canal, fecha)
 * DO UPDATE SET ultimo_folio = registro_folios.ultimo_folio + 1
 * RETURNING ultimo_folio
 * 
 * Esto:
 *   - Si no existe registro para hoy: crea con valor 1
 *   - Si ya existe: incrementa en 1
 *   - Retorna el valor final (para generar el folio)
 *   - Es ATÓMICO (evita que dos pedidos reciban el mismo número)
 * 
 * ================================================================================================================
 * EJEMPLO DE USO EN SERVICIO
 * ================================================================================================================
 * 
 * public String nextFolioDiario(String canal) {
 *     // Ejecutar query atómica
 *     int numero = queryAtomic(canal, LocalDate.now());
 *     
 *     // Formatear: NORTE-260327-0045
 *     return String.format("%s-%s-%04d", 
 *         canal,
 *         LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")),
 *         numero
 *     );
 * }
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "registro_folios", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_canal_fecha", columnNames = {"canal", "fecha"})
    }
)
@Getter @Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class RegistroFolio {

    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // IDENTIFICACIÓN DEL CONTADOR
    // =====================================================
    
    /**
     * 🔖 CANAL o COMEDOR que genera el folio
     * Ejemplos: "NORTE", "SUR", "CENTRAL", "PLANTA1", "PLANTA2"
     * Cada comedor tiene su propia secuencia independiente
     */
    @Column(nullable = false, length = 10)
    private String canal;
    
    /**
     * 📅 FECHA del contador
     * Cada día reinicia la secuencia (el folio del día 1 es 1, no 46)
     */
    @Column(nullable = false)
    private LocalDate fecha;
    
    // =====================================================
    // CONTADOR
    // =====================================================
    
    /**
     * 🔢 ÚLTIMO FOLIO generado para este canal y fecha
     * Para generar el siguiente: ultimo_folio + 1
     */
    @Column(name = "ultimo_folio", nullable = false)
    private Integer ultimoFolio;
    
    // =====================================================
    // CONTROL DE CONCURRENCIA
    // =====================================================
    
    /**
     * 🔐 VERSIÓN para control de concurrencia optimista
     * Incrementa automáticamente con cada actualización
     * Previene actualizaciones concurrentes conflictivas
     */
    @Version
    private Long version;
}