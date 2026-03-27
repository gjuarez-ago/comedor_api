package com.services.comedor.models;

import java.util.List;

public record PedidoRequest(Long empleadoId, Long comedorId, List<DetalleRequest> detalles) {}

