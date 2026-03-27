package com.services.comedor.models;

public record CancelarPedidoRequest(Long pedidoId, String motivo, Long usuarioCancelaId) {}

