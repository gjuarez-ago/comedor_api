package com.services.comedor.entity;

import com.services.comedor.enums.EstadoConsumo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📜 ENTIDAD: CONSUMO_ESTADO_HISTORIAL
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Esta tabla registra CADA cambio de estado que sufre un consumo a lo largo de su ciclo de vida.
 * Es fundamental para AUDITORÍA y para conocer el historial completo de cada pedido.
 * 
 * ================================================================================================================
 * CICLO DE VIDA CON HISTORIAL
 * ================================================================================================================
 * 
 * Consumo #100:
 *   ├── [13:20] CREADO      (empleado generó pedido)
 *   ├── [13:22] PAGADO      (cajero escaneó QR)
 *   ├── [13:25] PREPARANDO  (cocinero tomó el pedido)
 *   ├── [13:28] LISTO       (cocinero terminó)
 *   └── [13:30] ENTREGADO   (cajero entregó)
 * 
 * Cada uno de estos cambios queda registrado en esta tabla.
 * 
 * ================================================================================================================
 * UTILIDADES
 * ================================================================================================================
 * 
 * 1. Auditoría: saber quién procesó cada paso y cuándo
 * 2. Resolución de disputas: si un empleado reclama, saber qué pasó
 * 3. Métricas: calcular tiempo promedio entre estados
 * 4. Reportes: análisis de cuellos de botella
 * 5. Depuración: rastrear problemas en producción
 * 
 * ================================================================================================================
 * EJEMPLO DE DATOS
 * ================================================================================================================
 * 
 * | id | consumo_id | estado      | usuario_id | fecha                |
 * |----|------------|-------------|------------|----------------------|
 * | 1  | 100        | CREADO      | NULL       | 2026-03-28 13:20:00  |
 * | 2  | 100        | PAGADO      | 1 (cajero) | 2026-03-28 13:22:00  |
 * | 3  | 100        | PREPARANDO  | 2 (cocina) | 2026-03-28 13:25:00  |
 * | 4  | 100        | LISTO       | 2 (cocina) | 2026-03-28 13:28:00  |
 * | 5  | 100        | ENTREGADO   | 1 (cajero) | 2026-03-28 13:30:00  |
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "consumo_estado_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumoEstadoHistorial {
    
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
     * 🧾 CONSUMO al que pertenece este registro de historial
     * Relación Many-to-One: un consumo puede tener muchos historiales
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_id")
    private Consumo consumo;
    
    /**
     * 🔄 ESTADO en el momento del registro
     * Posibles valores: CREADO, PAGADO, PREPARANDO, LISTO, ENTREGADO, CANCELADO
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoConsumo estado;
    
    /**
     * 👤 USUARIO que realizó el cambio de estado
     * - Puede ser NULL cuando es creación automática (empleado)
     * - En otros casos: cajero, cocinero, administrador
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    // =====================================================
    // FECHA Y HORA
    // =====================================================
    
    /**
     * ⏰ FECHA Y HORA en que ocurrió el cambio de estado
     * Se asigna automáticamente al persistir
     */
    @Column(updatable = false)
    private LocalDateTime fecha;
    
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