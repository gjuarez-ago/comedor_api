package com.services.comedor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "producto_stock",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"producto_id", "comedor_id"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    @Column(name = "stock_actual", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stockActual;
}

