package com.services.comedor.models;

import java.util.List;

public record ComandaKdsResponse(Long pedidoId, String folio, String hora, List<DetalleKdsDTO> items) {}

