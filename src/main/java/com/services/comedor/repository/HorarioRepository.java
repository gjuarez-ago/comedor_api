package com.services.comedor.repository;

import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.cache.annotation.Cacheable;

import com.services.comedor.entity.Horario;
import com.services.comedor.entity.TipoConsumo;

public interface HorarioRepository extends JpaRepository<Horario, Long> {


    // En HorarioRepository.java

/**
 * Busca horario activo por comedor, día de semana y hora actual
 */
 /**
     * Busca horario activo por comedor, día de semana y hora actual
     * 🔥 CACHE: clave = comedorId + día + hora (redondeada al minuto)
     */
    @Cacheable(value = "horarios", key = "#comedorId + '_' + #diaSemana + '_' + T(java.time.LocalTime).of(#horaActual.hour, #horaActual.minute, 0)")
    @Query("SELECT h FROM Horario h " +
           "WHERE h.comedor.id = :comedorId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.activo = true " +
           "AND :horaActual BETWEEN h.horaInicio AND h.horaFin")
    Optional<Horario> findActiveByDayAndTime(
            @Param("comedorId") Long comedorId,
            @Param("diaSemana") Integer diaSemana,
            @Param("horaActual") LocalTime horaActual);

            
    // 📱 APP y 💻 POS
    // Recibe la hora actual del servidor y te dice si es Desayuno, Comida o Cena.
    @Query("SELECT h.tipoConsumo FROM Horario h " +
           "WHERE h.comedor.id = :comedorId " +
           "AND :hora BETWEEN h.horaInicio AND h.horaFin")
    Optional<TipoConsumo> findTipoConsumoActual(
            @Param("comedorId") Long comedorId, 
            @Param("hora") LocalTime hora
    );

    @Query("SELECT h FROM Horario h " +
           "WHERE h.comedor.id = :comedorId " +
           "AND h.activo = true " +
           "AND :hora BETWEEN h.horaInicio AND h.horaFin")
    Optional<Horario> findActiveByTime(@Param("comedorId") Long comedorId, 
                                        @Param("hora") LocalTime hora);

    /**
     * Busca horario por comedor y tipo de consumo.
     */
    Optional<Horario> findByComedorIdAndTipoConsumoIdAndActivoTrue(
        Long comedorId, Long tipoConsumoId
    );

    /**
     * ✅ VALIDACIÓN RÁPIDA: Verifica si existe horario activo para un tipo en un comedor.
     * Usa COUNT, es más rápido que cargar la entidad completa.
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h " +
           "WHERE h.comedor.id = :comedorId " +
           "AND h.tipoConsumo.id = :tipoConsumoId " +
           "AND h.activo = true " +
           "AND :horaActual BETWEEN h.horaInicio AND h.horaFin")
    boolean existeHorarioActivo(
        @Param("comedorId") Long comedorId,
        @Param("tipoConsumoId") Long tipoConsumoId,
        @Param("horaActual") LocalTime horaActual
    );

    // =====================================================
    // 🔥 NUEVO MÉTODO PARA VERIFICAR SI EXISTEN HORARIOS ACTIVOS
    // =====================================================

    /**
     * ✅ VERIFICACIÓN RÁPIDA: Verifica si existe al menos un horario activo en el sistema.
     * Usa COUNT, es más rápido que cargar entidades.
     * Útil para mantenimiento semanal y validaciones de integridad.
     * 
     * @return true si existe al menos un horario activo
     */
    @Query("SELECT COUNT(h) > 0 FROM Horario h WHERE h.activo = true")
    boolean existsByActivoTrue();
}