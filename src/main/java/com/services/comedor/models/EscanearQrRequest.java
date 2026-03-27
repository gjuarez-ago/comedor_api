package com.services.comedor.models;

public record EscanearQrRequest(String qrToken, Long comedorTabletId, Long usuarioCajeroId) {}

