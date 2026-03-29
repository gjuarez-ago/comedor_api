package com.services.comedor.services;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class FolioService {

    @PersistenceContext
    private EntityManager entityManager;

    // Formateador para que el folio tenga la fecha (Ej: 260327 para 27-Marzo-2026)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextFolioDiario(String canal) {
        if (canal == null || canal.trim().isEmpty()) {
            throw new IllegalArgumentException("Canal (Comedor) requerido");
        }

        String prefijo = canal.trim().toUpperCase();
        LocalDate hoy = LocalDate.now();

        // 🔥 SQL NATIVO ATÓMICO: Insert o Update y devuelve el valor final en 1 paso
        Number ultimoFolio = (Number) entityManager.createNativeQuery("""
                INSERT INTO registro_folios (canal, fecha, ultimo_folio)
                VALUES (:canal, :fecha, 1)
                ON CONFLICT (canal, fecha)
                DO UPDATE SET ultimo_folio = registro_folios.ultimo_folio + 1
                RETURNING ultimo_folio
                """)
                .setParameter("canal", prefijo)
                .setParameter("fecha", java.sql.Date.valueOf(hoy))
                .getSingleResult();

        // Resultado: NTE-260327-0045
        // (Prefijo - Fecha corta - Folio con ceros a la izquierda para que se vea ordenado)
        return String.format("%s-%s-%04d", 
                prefijo, 
                hoy.format(DATE_FORMAT), 
                ultimoFolio.intValue());
    }
}