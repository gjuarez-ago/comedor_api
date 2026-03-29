package com.services.comedor.models;

import java.util.List;

public record ConfiguracionInicialProductoDTO(
        List<ComboComedorDTO> comedores,
        List<ComboTipoConsumoDTO> tiposConsumo,
        List<ComboDiaSemanaDTO> diasSemana
) {}

