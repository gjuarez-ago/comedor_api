package com.services.comedor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "requiere_preparacion", columnDefinition = "boolean default false")
    private Boolean requierePreparacion;

    @Column(name = "controla_inventario", columnDefinition = "boolean default false")
    private Boolean controlaInventario;

    @Column(columnDefinition = "boolean default true")
    private Boolean activo;

    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;
}

