package com.services.comedor.models;

public record LoginUsuarioResponse(Long usuarioId, String nombreCompleto, String rol, String tokenJwt) {}

