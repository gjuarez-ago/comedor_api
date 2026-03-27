package com.services.comedor.models;

import java.util.List;

public record PanelDespachoResponse(List<FilaDespachoDTO> prioridadAlta, List<FilaDespachoDTO> enEspera) {}

