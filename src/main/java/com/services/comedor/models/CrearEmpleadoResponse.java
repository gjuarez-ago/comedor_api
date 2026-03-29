package com.services.comedor.models;

import java.util.List;

public record CrearEmpleadoResponse(
    Long id,
    String numeroEmpleado,
    String nombre,
    String telefono,
    String comedor,
    Boolean horarioFlexible,
    List<String> tiposConsumoPermitidos,
    Boolean activo,
    String mensaje,
    String pinTemporal,
    String qrCodeUrl
) {}