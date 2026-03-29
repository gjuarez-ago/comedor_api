package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 📅 ENTIDAD: PRODUCTO_DIAS_DISPONIBLES
 * 
 * Define qué días de la semana está disponible un producto en un comedor específico.
 * Si no existe registro para un producto/comedor/día, se considera DISPONIBLE por defecto.
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "producto_dias_disponibles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDiaDisponible {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 🍲 PRODUCTO al que pertenece esta configuración
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /**
     * 🏭 COMEDOR donde aplica esta configuración
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    /**
     * 📅 DÍA DE LA SEMANA
     * 1 = Lunes
     * 2 = Martes
     * 3 = Miércoles
     * 4 = Jueves
     * 5 = Viernes
     * 6 = Sábado
     * 7 = Domingo
     */
    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    /**
     * 🟢 DISPONIBILIDAD en este día
     * true = disponible
     * false = no disponible (se oculta del menú)
     */
    @Column(columnDefinition = "boolean default true")
    private Boolean disponible;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}