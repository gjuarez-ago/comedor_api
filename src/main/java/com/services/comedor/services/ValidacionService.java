package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.repository.ConsumoRepository;
import com.services.comedor.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ValidacionService {

    private final HorarioRepository horarioRepository;
    private final ConsumoRepository consumoRepository;

    private static final Long TIPO_TIENDA_ID = 99L;

    /**
     * Validar horario de consumo
     * - Snacks (TIENDA) no tienen restricción
     * - Comidas validan contra horario del comedor
     */
    public void validarHorario(Empleado empleado, TipoConsumo tipoConsumo, LocalDateTime momento) {
        
        // Snacks no tienen restricción de horario
        if (TIPO_TIENDA_ID.equals(tipoConsumo.getId())) {
            return;
        }

        Horario horario = horarioRepository.findByComedorIdAndTipoConsumoIdAndActivoTrue(
                empleado.getComedor().getId(), 
                tipoConsumo.getId())
                .orElseThrow(() -> new BusinessException(
                        "HOR_001", 
                        "No hay horario definido para " + tipoConsumo.getNombre(),
                        HttpStatus.NOT_FOUND));

        LocalTime horaActual = momento.toLocalTime();
        LocalTime horaInicio = horario.getHoraInicio();
        LocalTime horaFin = horario.getHoraFin();

        boolean dentroHorario = !horaActual.isBefore(horaInicio) && !horaActual.isAfter(horaFin);

        if (!dentroHorario) {
            throw new BusinessException(
                    "HOR_002",
                    "Horario de " + tipoConsumo.getNombre() + ": " + horaInicio + " - " + horaFin,
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validar que el empleado no haya consumido este tipo hoy
     */
    public void validarDobleConsumo(Long empleadoId, Long tipoConsumoId) {
        // 🔥 CORREGIDO: Pasar la fecha actual en lugar de null
        boolean yaConsumio = consumoRepository.yaConsumioHoy(
                empleadoId, 
                tipoConsumoId, 
                LocalDate.now()  // ← Fecha actual
        );
        
        if (yaConsumio) {
            throw new BusinessException(
                    "CON_001",
                    "Ya consumiste este servicio hoy",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validar límite de comidas para administrativos (máximo 2 por día)
     */
    // En ValidacionService.java

/**
 * Validar límite de comidas para administrativos (máximo 2 por día)
 */
public void validarLimiteDiarioAdministrativo(Long empleadoId) {
    int consumidos = consumoRepository.countTiposConsumidosHoy(empleadoId);
    
    if (consumidos >= 2) {
        throw new BusinessException(
                "CON_002",
                "Ya consumiste tus 2 comidas del día",
                HttpStatus.BAD_REQUEST);
    }
}

    /**
     * Validar que el QR no haya expirado
     * - Snacks: 30 minutos de vigencia
     * - Comidas: hasta el fin del horario + 30 min de tolerancia
     */
    public void validarVigenciaQR(Consumo consumo, LocalDateTime momento) {
        
        if (TIPO_TIENDA_ID.equals(consumo.getTipoConsumo().getId())) {
            // Snacks: vigencia 30 minutos
            LocalDateTime expiracion = consumo.getFechaCreacion().plusMinutes(30);
            if (momento.isAfter(expiracion)) {
                throw new BusinessException(
                        "CON_010",
                        "QR expirado. Genera uno nuevo",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            // Comidas: vigencia hasta fin del horario + 30 min
            Horario horario = horarioRepository
                    .findByComedorIdAndTipoConsumoIdAndActivoTrue(
                            consumo.getComedor().getId(), 
                            consumo.getTipoConsumo().getId())
                    .orElseThrow(() -> new BusinessException(
                            "HOR_001", 
                            "Sin horario definido", 
                            HttpStatus.NOT_FOUND));
            
            LocalTime finHorario = horario.getHoraFin();
            LocalTime ahora = momento.toLocalTime();
            LocalTime tolerancia = finHorario.plusMinutes(30);
            
            if (ahora.isAfter(tolerancia)) {
                throw new BusinessException(
                        "CON_010",
                        "QR expirado. El horario de " + consumo.getTipoConsumo().getNombre() + 
                        " terminó a las " + finHorario,
                        HttpStatus.BAD_REQUEST);
            }
        }
    }
}