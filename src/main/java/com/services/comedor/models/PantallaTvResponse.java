package com.services.comedor.models;

import java.util.List;

public record PantallaTvResponse(List<TicketTvDTO> enPreparacion, List<TicketTvDTO> listosParaRecoger) {}

