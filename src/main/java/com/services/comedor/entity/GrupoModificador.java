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

@Entity
@Table(name = "grupos_modificadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoModificador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(length = 80)
    private String nombre;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer minimo;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer maximo;
}

