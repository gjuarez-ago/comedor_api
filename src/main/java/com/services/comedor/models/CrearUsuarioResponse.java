package com.services.comedor.models;

import java.util.List;

public record CrearUsuarioResponse(
    Long id,
    String username,
    String nombre,
    String rol,
    String comedorBase,
    List<String> comedoresPermitidos,
    Boolean activo,
    String mensaje,
    String pinTemporal
) {}