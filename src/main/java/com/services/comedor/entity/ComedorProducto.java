package com.services.comedor.entity;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "comedor_productos",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"comedor_id", "producto_id"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComedorProducto {
        
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(
            name = "precio_empleado",
            precision = 10,
            scale = 2,
            columnDefinition = "DECIMAL(10,2) DEFAULT 0.00"
    )
    private BigDecimal precioEmpleado;

    @Column(
            name = "precio_empresa",
            precision = 10,
            scale = 2,
            columnDefinition = "DECIMAL(10,2) DEFAULT 0.00"
    )
    private BigDecimal precioEmpresa;

    @Column(columnDefinition = "boolean default true")
    private Boolean disponible;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "comedor_producto_turnos", // El nombre de la tabla intermedia en BD
        joinColumns = @JoinColumn(name = "comedor_producto_id"), // La llave de esta tabla
        inverseJoinColumns = @JoinColumn(name = "tipo_consumo_id") // La llave de la tabla destino
    )
    private Set<TipoConsumo> turnosDisponibles;
}

