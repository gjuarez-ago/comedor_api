package com.services.comedor.entity;

import com.services.comedor.enums.EstadoConsumo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Set;

/**
 * 🧾 ENTIDAD: CONSUMO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Esta es la tabla MÁS IMPORTANTE del sistema. Registra CADA transacción de consumo realizada por un empleado.
 * Cada vez que un empleado genera un pedido, se crea un registro aquí que evoluciona a través de diferentes
 * estados hasta que la comida es entregada o cancelada.
 * 
 * ================================================================================================================
 * CICLO DE VIDA DE UN CONSUMO
 * ================================================================================================================
 * 
 * ┌─────────┐    ┌─────────┐    ┌────────────┐    ┌──────┐    ┌───────────┐
 * │ CREADO  │───►│ PAGADO  │───►│ PREPARANDO │───►│ LISTO│───►│ ENTREGADO │
 * └─────────┘    └─────────┘    └────────────┘    └──────┘    └───────────┘
 *      │              │               │              │
 *      │              │               │              │
 *      └──────────────┴───────────────┴──────────────┘
 *                              │
 *                              ▼
 *                        ┌──────────┐
 *                        │CANCELADO │
 *                        └──────────┘
 * 
 * ================================================================================================================
 * REGLAS DE NEGOCIO IMPORTANTES
 * ================================================================================================================
 * 
 * 1. Un empleado NO puede tener más de un consumo activo del mismo tipo en el mismo día (UniqueConstraint)
 * 2. Los snacks (tipo_consumo_id=99) NO tienen límite diario
 * 3. Los consumos forzados (forzado=true) son excepciones autorizadas por supervisor
 * 4. Si un consumo se cancela en estado PREPARANDO o LISTO, se marca como merma (esMerma=true)
 * 5. El QR token es único y se usa para validar el pedido en caja
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "consumos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empleado_id", "tipo_consumo_id", "fecha"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumo {
    
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
     * 👤 EMPLEADO que realiza el consumo
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;
    
    /**
     * 🏭 COMEDOR donde se realiza el consumo (auditoría importante)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;
    
    /**
     * 🍽️ TIPO DE CONSUMO (Desayuno, Comida, Cena, Tienda)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_consumo_id", nullable = false)
    private TipoConsumo tipoConsumo;
    
    // =====================================================
    // ESTADO DEL PEDIDO
    // =====================================================
    
    /**
     * 🔄 ESTADO ACTUAL del consumo
     * Posibles valores: CREADO, PAGADO, PREPARANDO, LISTO, ENTREGADO, CANCELADO
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConsumo estado;
    
    /**
     * 🔐 TOKEN QR único para identificar este consumo
     * Se genera al crear el pedido y se usa para validar en caja
     */
    @Column(name = "token_qr", unique = true, length = 100)
    private String tokenQr;
    
    // =====================================================
    // FECHAS
    // =====================================================
    
    /**
     * 📅 FECHA del consumo (para validación de doble consumo)
     */
    @Column(nullable = false)
    private LocalDate fecha;
    
    /**
     * ⏰ FECHA Y HORA de creación del pedido
     */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    /**
     * ❌ FECHA Y HORA de cancelación (si aplica)
     */
    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;
    
    // =====================================================
    // EXCEPCIONES Y FORZADOS
    // =====================================================
    
    /**
     * 🚨 INDICA si este consumo fue FORZADO por un supervisor
     * true = consumo fuera de regla autorizado (ej: llegó tarde, doble consumo excepcional)
     */
    @Column(name = "forzado", columnDefinition = "boolean default false")
    private Boolean forzado;
    
    /**
     * 📝 MOTIVO del forzado (ej: "Llegó tarde por tráfico")
     */
    @Column(name = "motivo_forzado", length = 255)
    private String motivoForzado;
    
    /**
     * 💬 MOTIVO de cancelación (ej: "Cliente no llegó", "Falta de ingredientes")
     */
    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;
    
    /**
     * 🏪 MOTIVO de venta directa (cuando el cajero crea el pedido sin QR)
     */
    @Column(name = "motivo_directo", length = 255)
    private String motivoDirecto;
    
    /**
     * 🗑️ MARCA DE MERMA: indica si la comida se perdió
     * true = pedido cancelado cuando ya estaba en PREPARANDO o LISTO
     */
    @Column(name = "es_merma", columnDefinition = "boolean default false")
    private Boolean esMerma;
    
    // =====================================================
    // AUDITORÍA DE USUARIOS
    // =====================================================
    
    /**
     * 👨‍💼 USUARIO que validó el pedido (cajero que escaneó el QR)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_valida_id")
    private Usuario usuarioValida;
    
    /**
     * ❌ USUARIO que canceló el pedido
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cancela_id")
    private Usuario usuarioCancela;
    
    /**
     * 🔑 USUARIO que autorizó un consumo forzado (supervisor)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_autoriza_id")
    private Usuario usuarioAutoriza;
    
    // =====================================================
    // DETALLES DEL PEDIDO
    // =====================================================
    
    /**
     * 📋 DETALLES del consumo (productos, cantidades, modificadores)
     * Relación One-to-Many: un consumo puede tener múltiples productos
     */
    @OneToMany(mappedBy = "consumo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConsumoDetalle> detalles;
    
    // =====================================================
    // LÓGICA DE INICIALIZACIÓN
    // =====================================================
    
    /**
     * 🔧 Se ejecuta ANTES de guardar el registro por primera vez
     * Asigna automáticamente fechaCreacion y fecha si no se especificaron
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }
}