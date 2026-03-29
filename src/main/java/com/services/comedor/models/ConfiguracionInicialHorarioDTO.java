package com.services.comedor.models;

import java.util.List;

public record ConfiguracionInicialHorarioDTO(
        List<ComboComedorDTO> comedores,
        List<ComboTipoConsumoDTO> tiposConsumo,
        List<ComboDiaSemanaDTO> diasSemana
) {}

