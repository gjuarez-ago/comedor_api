package com.services.comedor.entity;

import java.math.BigDecimal;

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

@Entity
@Table(name = "consumo_detalle_modificadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumoDetalleModificador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_id", nullable = false)
    private ConsumoDetalle consumoDetalle;

    @Column(name = "nombre_opcion", nullable = false, length = 100)
    private String nombreOpcion;

    private BigDecimal precioExtra; // Por si el extra cuesta (ej: tocino)

    // En ConsumoDetalleModificador.java
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsumoDetalleModificador)) {
            return false;
        }
        ConsumoDetalleModificador that = (ConsumoDetalleModificador) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
}
