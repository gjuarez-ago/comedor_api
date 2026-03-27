package com.services.comedor.entity;

import com.services.comedor.enums.EstadoConsumo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "consumos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empleado_id", "tipo_consumo_id", "fecha"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 FALTABA: Quién es el comensal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    // 🔥 FALTABA: Si es Desayuno, Comida, etc.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_consumo_id", nullable = false)
    private TipoConsumo tipoConsumo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConsumo estado;

    // 🔥 FALTABA: El token de seguridad
    @Column(name = "token_qr", unique = true, length = 100)
    private String tokenQr;

    // 🔥 FALTABA: La fecha del día para validar que no coma doble
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // 🔥 FALTABA: Auditoría de usuarios
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_valida_id")
    private Usuario usuarioValida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cancela_id")
    private Usuario usuarioCancela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_autoriza_id") // Permite nulos
    private Usuario usuarioAutoriza;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "motivo_directo", length = 255)
    private String motivoDirecto;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "es_merma", columnDefinition = "boolean default false")
    private Boolean esMerma;

    // 💥 EL CULPABLE DEL ERROR: La relación con los platillos del ticket
    @OneToMany(mappedBy = "consumo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConsumoDetalle> detalles;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        // Si no se asignó fecha manual, tomamos la de hoy por defecto
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }
}