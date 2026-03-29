package com.services.comedor.models;

import java.util.List;

public record ConfiguracionInicialEmpleadoDTO(
        List<ComboComedorDTO> comedores,
        List<ComboTipoConsumoDTO> tiposConsumo
) {}

