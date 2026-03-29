package com.services.comedor.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DiaSemana {
    LUNES(1, "LUNES"),
    MARTES(2, "MARTES"),
    MIERCOLES(3, "MIERCOLES"),
    JUEVES(4, "JUEVES"),
    VIERNES(5, "VIERNES"),
    SABADO(6, "SABADO"),
    DOMINGO(7, "DOMINGO");

    private final int numero;
    private final String nombre;

    private static final Map<Integer, DiaSemana> POR_NUMERO =
            Arrays.stream(values()).collect(Collectors.toMap(d -> d.numero, d -> d));

    DiaSemana(int numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public static DiaSemana fromNumero(int numero) {
        return POR_NUMERO.get(numero);
    }
}
