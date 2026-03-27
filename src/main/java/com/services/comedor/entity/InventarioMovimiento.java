package com.services.comedor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "inventario_movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioMovimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_id")
    private Consumo consumo;

    @Column(length = 10)
    private String tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(updatable = false)
    private LocalDateTime fecha;

    @Column(length = 100)
    private String motivo;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}

