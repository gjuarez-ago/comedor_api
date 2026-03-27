package com.services.comedor.models;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumoEstatusResponse {
    private Long id;
    private String folio;
    private String estado; // CREADO, PAGADO, PREPARANDO, LISTO
    private String mensaje; // Ej: "Tu comida se está preparando"
    private Integer posicionEnFila; // Opcional: ¿Cuántos hay antes que él?
    private String tokenQr; // Por si necesita volver a mostrarlo en barra
}