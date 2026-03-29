package com.services.comedor.models;

import java.time.LocalDateTime;

/**
 * DTO para merma (sin cargar entidades completas)
 */
public class MermaDTO {
    
    private final Long id;
    private final String qrToken;
    private final LocalDateTime fechaCreacion;
    private final Long empleadoId;
    private final String empleadoNombre;
    private final String empleadoNumero;
    private final Long tipoConsumoId;
    private final String tipoConsumoNombre;
    private final Long cantidadProductos;

    // 🔥 CONSTRUCTOR COMPLETO
    public MermaDTO(
            Long id,
            String qrToken,
            LocalDateTime fechaCreacion,
            Long empleadoId,
            String empleadoNombre,
            String empleadoNumero,
            Long tipoConsumoId,
            String tipoConsumoNombre,
            Long cantidadProductos) {
        this.id = id;
        this.qrToken = qrToken;
        this.fechaCreacion = fechaCreacion;
        this.empleadoId = empleadoId;
        this.empleadoNombre = empleadoNombre;
        this.empleadoNumero = empleadoNumero;
        this.tipoConsumoId = tipoConsumoId;
        this.tipoConsumoNombre = tipoConsumoNombre;
        this.cantidadProductos = cantidadProductos;
    }

    // Getters
    public Long getId() { return id; }
    public String getQrToken() { return qrToken; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public Long getEmpleadoId() { return empleadoId; }
    public String getEmpleadoNombre() { return empleadoNombre; }
    public String getEmpleadoNumero() { return empleadoNumero; }
    public Long getTipoConsumoId() { return tipoConsumoId; }
    public String getTipoConsumoNombre() { return tipoConsumoNombre; }
    public Long getCantidadProductos() { return cantidadProductos; }
}