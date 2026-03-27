package com.services.comedor.models;

public record LoginResponse(Long id, String nombre, String rol, Long comedorId, String token) {}

