package com.services.comedor.models.admin;

import java.util.List;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String rol,
        Long comedorBaseId,
        String comedorBaseNombre,
        List<ComedorResponse> comedoresPermitidos,
        Boolean activo
) {}

