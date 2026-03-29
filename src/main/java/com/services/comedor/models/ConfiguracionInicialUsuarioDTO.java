package com.services.comedor.models;

import java.util.List;

public record ConfiguracionInicialUsuarioDTO(
        List<ComboComedorDTO> comedores,
        List<String> roles
) {}

