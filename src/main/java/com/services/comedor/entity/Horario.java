package com.services.comedor.entity;

import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * ⏰ ENTIDAD: HORARIO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Define los HORARIOS DE SERVICIO del comedor para cada tipo de comida.
 * Esta tabla responde a la pregunta: ¿CUÁNDO se sirve cada tipo de comida en cada comedor?
 * 
 * ================================================================================================================
 * EJEMPLO DE USO
 * ================================================================================================================
 * 
 * COMEDOR NORTE:
 *   - DESAYUNO: 07:00 - 09:00
 *   - COMIDA:   12:00 - 15:00
 *   - CENA:     18:00 - 20:00
 * 
 * COMEDOR SUR (horarios diferentes):
 *   - DESAYUNO: 06:30 - 08:30
 *   - COMIDA:   11:30 - 14:30
 *   - CENA:     17:30 - 19:30
 * 
 * ================================================================================================================
 * RELACIONES
 * ================================================================================================================
 * 
 * ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
 * │    COMEDOR      │     │    HORARIO      │     │  TIPO_CONSUMO   │
 * ├─────────────────┤     ├─────────────────┤     ├─────────────────┤
 * │ id (PK)         │────►│ comedor_id (FK) │     │ id (PK)         │
 * │ nombre          │     │ tipo_consumo_id │────►│ nombre          │
 * │ activo          │     │ hora_inicio     │     └─────────────────┘
 * └─────────────────┘     │ hora_fin        │
 *                         │ activo          │
 *                         └─────────────────┘
 * 
 * ================================================================================================================
 * REGLAS DE NEGOCIO
 * ================================================================================================================
 * 
 * 1. Snacks (tipo_consumo_id=99) NO tienen horario (siempre disponibles)
 * 2. Un comedor puede tener múltiples horarios (uno por tipo de consumo)
 * 3. Los horarios pueden estar activos o inactivos (para cierres temporales)
 * 4. hora_inicio DEBE ser menor que hora_fin
 * 5. Si no hay horario para un tipo, ese tipo NO está disponible
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "horarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Horario {
    
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
     * 🏭 COMEDOR al que pertenece este horario
     * Relación Many-to-One: un comedor puede tener múltiples horarios
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id")
    private Comedor comedor;
    
    /**
     * 🍽️ TIPO DE CONSUMO que se sirve en este horario
     * Valores posibles: DESAYUNO(1), COMIDA(2), CENA(3)
     * TIENDA(99) NO tiene horario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_consumo_id")
    private TipoConsumo tipoConsumo;
    
    /**
    * 📅 DÍA DE LA SEMANA
    * 1 = Lunes, 2 = Martes, ..., 7 = Domingo
    */
    @Column(name = "dia_semana")
    private Integer diaSemana;

    // =====================================================
    // HORARIOS
    // =====================================================
    
    /**
     * 🕐 HORA DE INICIO del servicio
     * Ejemplos: 07:00, 12:00, 18:00
     */
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;
    
    /**
     * 🕐 HORA DE FIN del servicio
     * Ejemplos: 09:00, 15:00, 20:00
     * IMPORTANTE: debe ser mayor que hora_inicio
     */
    @Column(name = "hora_fin")
    private LocalTime horaFin;
    
    // =====================================================
    // ESTADO
    // =====================================================
    
    /**
     * 🟢 ACTIVO/INACTIVO del horario
     * - true: el horario está vigente
     * - false: el horario está deshabilitado (útil para cierres temporales)
     */
    @Column(name = "activo")
    private boolean activo;
}