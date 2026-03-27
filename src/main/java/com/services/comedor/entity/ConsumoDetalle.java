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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "consumo_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumo_id", nullable = false)
    private Consumo consumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;


    @Column(name = "precio_unitario_empleado", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioEmpleado;

    @Column(name = "precio_unitario_empresa", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioEmpresa;

    @OneToMany(mappedBy = "consumoDetalle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsumoDetalleModificador> modificadores;

    public BigDecimal getSubtotalEmpleado() {
        if (precioUnitarioEmpleado == null || cantidad == null) return BigDecimal.ZERO;
        return precioUnitarioEmpleado.multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal getSubtotalEmpresa() {
        if (precioUnitarioEmpresa == null || cantidad == null) return BigDecimal.ZERO;
        return precioUnitarioEmpresa.multiply(BigDecimal.valueOf(cantidad));
    }

    
}

