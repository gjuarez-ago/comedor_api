package com.services.comedor.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CacheInvalidationService {

    // =====================================================
    // INVALIDACIÓN DE CACHE DE MENÚ (por comedor y día)
    // =====================================================

    /**
     * Invalida el menú de un comedor para un día específico
     * 
     * @param comedorId ID del comedor
     * @param diaSemana Día de la semana (1=Lunes, 2=Martes, ..., 7=Domingo)
     */
    @CacheEvict(value = "menu", key = "#comedorId + '_' + #diaSemana")
    public void evictMenuCacheByDay(Long comedorId, Integer diaSemana) {
        // Método vacío, solo para invalidar cache
    }

    /**
     * Invalida el menú de un comedor para TODOS los días
     * 
     * @param comedorId ID del comedor
     */
    @CacheEvict(value = "menu", key = "#comedorId + '_*'", allEntries = true)
    public void evictMenuCacheAllDays(Long comedorId) {
        // Método vacío, solo para invalidar cache
    }

    /**
     * Invalida el menú de un comedor específico (mantiene compatibilidad)
     * 
     * @param comedorId ID del comedor
     */
    @CacheEvict(value = "menu", key = "#comedorId + '_' + T(java.time.LocalDate).now().getDayOfWeek().getValue()")
    public void evictMenuCacheToday(Long comedorId) {
        // Invalida solo el día de hoy
    }

    /**
     * Invalida TODOS los menús de TODOS los comedores y TODOS los días
     */
    @CacheEvict(value = "menu", allEntries = true)
    public void evictAllMenuCache() {
        // Método vacío, solo para invalidar cache
    }

    // =====================================================
    // INVALIDACIÓN DE CACHE DE HORARIOS (con días)
    // =====================================================

    /**
     * Invalida los horarios de un comedor para un día específico
     * 
     * @param comedorId ID del comedor
     * @param diaSemana Día de la semana (1=Lunes, 2=Martes, ..., 7=Domingo)
     */
    @CacheEvict(value = "horarios", key = "#comedorId + '_' + #diaSemana + '_*'", allEntries = true)
    public void evictHorariosCacheByDay(Long comedorId, Integer diaSemana) {
        // Método vacío, solo para invalidar cache
    }

    /**
     * Invalida TODOS los horarios de un comedor
     * 
     * @param comedorId ID del comedor
     */
    @CacheEvict(value = "horarios", key = "#comedorId + '_*'", allEntries = true)
    public void evictHorariosCacheByComedor(Long comedorId) {
        // Método vacío, solo para invalidar cache
    }

    /**
     * Invalida TODOS los horarios del sistema
     */
    @CacheEvict(value = "horarios", allEntries = true)
    public void evictAllHorariosCache() {
        // Método vacío, solo para invalidar cache
    }
}