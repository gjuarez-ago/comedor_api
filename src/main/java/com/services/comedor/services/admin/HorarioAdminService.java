package com.services.comedor.services.admin;

import com.services.comedor.entity.Comedor;
import com.services.comedor.entity.Horario;
import com.services.comedor.entity.TipoConsumo;
import com.services.comedor.enums.DiaSemana;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.CopiarHorariosRequest;
import com.services.comedor.models.admin.CrearHorarioRequest;
import com.services.comedor.models.admin.HorarioResponse;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.HorarioRepository;
import com.services.comedor.repository.TipoConsumoRepository;
import com.services.comedor.services.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HorarioAdminService {

    private final HorarioRepository horarioRepository;
    private final ComedorRepository comedorRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final CacheInvalidationService cacheInvalidationService;

    private static final Long TIPO_TIENDA_ID = 99L;

    // =====================================================
    // CRUD - LECTURA
    // =====================================================

    /**
     * 📋 Listar todos los horarios
     */
    @Transactional(readOnly = true)
    public List<HorarioResponse> listarTodos() {
        return horarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * 📋 Listar horarios por comedor
     */
    @Transactional(readOnly = true)
    public List<HorarioResponse> listarPorComedor(Long comedorId) {
        // Validar que el comedor exista
        comedorRepository.findById(comedorId)
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        return horarioRepository.findAll().stream()
                .filter(h -> h.getComedor().getId().equals(comedorId))
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * 📋 Listar horarios por comedor y día
     */
    @Transactional(readOnly = true)
    public List<HorarioResponse> listarPorComedorYDia(Long comedorId, Integer diaSemana) {
        // Validar día válido
        if (diaSemana < 1 || diaSemana > 7) {
            throw new BusinessException("HOR_007", "Día de semana inválido (1-7)", HttpStatus.BAD_REQUEST);
        }

        // Validar que el comedor exista
        comedorRepository.findById(comedorId)
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        return horarioRepository.findAll().stream()
                .filter(h -> h.getComedor().getId().equals(comedorId))
                .filter(h -> h.getDiaSemana().equals(diaSemana))
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🔍 Buscar horario por ID
     */
    @Transactional(readOnly = true)
    public HorarioResponse buscarPorId(Long id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOR_001", "Horario no encontrado", HttpStatus.NOT_FOUND));
        return convertirAResponse(horario);
    }

    // =====================================================
    // CRUD - ESCRITURA
    // =====================================================

    /**
     * ✨ Crear nuevo horario
     */
    @Transactional
    public HorarioResponse crear(CrearHorarioRequest request) {
        // 1. Validar que no sea TIENDA
        if (TIPO_TIENDA_ID.equals(request.tipoConsumoId())) {
            throw new BusinessException("HOR_004", "TIENDA no tiene horario", HttpStatus.BAD_REQUEST);
        }

        // 2. Validar hora inicio < hora fin
        if (request.horaInicio().isAfter(request.horaFin()) || request.horaInicio().equals(request.horaFin())) {
            throw new BusinessException("HOR_002", "Hora inicio debe ser menor que hora fin", HttpStatus.BAD_REQUEST);
        }

        // 3. Validar día válido
        if (request.diaSemana() < 1 || request.diaSemana() > 7) {
            throw new BusinessException("HOR_007", "Día de semana inválido (1-7)", HttpStatus.BAD_REQUEST);
        }

        // 4. Validar existencia de comedor
        Comedor comedor = comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        // 5. Validar existencia de tipo de consumo
        TipoConsumo tipo = tipoConsumoRepository.findById(request.tipoConsumoId())
                .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND));

        // 6. Validar que no exista duplicado
        boolean existe = horarioRepository.findAll().stream()
                .anyMatch(h -> h.getComedor().getId().equals(comedor.getId())
                        && h.getTipoConsumo().getId().equals(tipo.getId())
                        && h.getDiaSemana().equals(request.diaSemana()));

        if (existe) {
            throw new BusinessException("HOR_005", "Ya existe un horario para este comedor, tipo y día", HttpStatus.CONFLICT);
        }

        // 7. Crear horario
        Horario horario = Horario.builder()
                .comedor(comedor)
                .tipoConsumo(tipo)
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        horario = horarioRepository.save(horario);

        // 8. Invalidar cache
        cacheInvalidationService.evictHorariosCacheByDay(comedor.getId(), request.diaSemana());
        cacheInvalidationService.evictMenuCacheByDay(comedor.getId(), request.diaSemana());

        // 9. Log
        DiaSemana dia = DiaSemana.fromNumero(request.diaSemana());
        log.info("Horario creado - ID: {}, Comedor: {}, Día: {}, Tipo: {}, Horario: {}-{}",
                horario.getId(),
                comedor.getNombre(),
                dia != null ? dia.getNombre() : request.diaSemana(),
                tipo.getNombre(),
                request.horaInicio(),
                request.horaFin());

        return convertirAResponse(horario);
    }

    /**
     * ✏️ Actualizar horario
     */
    @Transactional
    public HorarioResponse actualizar(Long id, CrearHorarioRequest request) {
        // 1. Buscar horario existente
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOR_001", "Horario no encontrado", HttpStatus.NOT_FOUND));

        // 2. Validar que no sea TIENDA
        if (TIPO_TIENDA_ID.equals(request.tipoConsumoId())) {
            throw new BusinessException("HOR_004", "TIENDA no tiene horario", HttpStatus.BAD_REQUEST);
        }

        // 3. Validar hora inicio < hora fin
        if (request.horaInicio().isAfter(request.horaFin()) || request.horaInicio().equals(request.horaFin())) {
            throw new BusinessException("HOR_002", "Hora inicio debe ser menor que hora fin", HttpStatus.BAD_REQUEST);
        }

        // 4. Validar día válido
        if (request.diaSemana() < 1 || request.diaSemana() > 7) {
            throw new BusinessException("HOR_007", "Día de semana inválido (1-7)", HttpStatus.BAD_REQUEST);
        }

        // 5. Validar comedor
        Comedor comedor = comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        // 6. Validar tipo
        TipoConsumo tipo = tipoConsumoRepository.findById(request.tipoConsumoId())
                .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND));

        // 7. Guardar datos antiguos para invalidación
        Long oldComedorId = horario.getComedor().getId();
        Integer oldDiaSemana = horario.getDiaSemana();

        // 8. Validar duplicado si cambia clave
        if (!horario.getComedor().getId().equals(comedor.getId())
                || !horario.getTipoConsumo().getId().equals(tipo.getId())
                || !horario.getDiaSemana().equals(request.diaSemana())) {

            boolean existe = horarioRepository.findAll().stream()
                    .anyMatch(h -> h.getComedor().getId().equals(comedor.getId())
                            && h.getTipoConsumo().getId().equals(tipo.getId())
                            && h.getDiaSemana().equals(request.diaSemana())
                            && !h.getId().equals(id));

            if (existe) {
                throw new BusinessException("HOR_005", "Ya existe un horario para este comedor, tipo y día", HttpStatus.CONFLICT);
            }
        }

        // 9. Actualizar
        horario.setComedor(comedor);
        horario.setTipoConsumo(tipo);
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        horario.setActivo(request.activo() != null ? request.activo() : horario.isActivo());

        horario = horarioRepository.save(horario);

        // 10. Invalidar cache (día anterior y nuevo)
        cacheInvalidationService.evictHorariosCacheByDay(oldComedorId, oldDiaSemana);
        cacheInvalidationService.evictMenuCacheByDay(oldComedorId, oldDiaSemana);
        cacheInvalidationService.evictHorariosCacheByDay(comedor.getId(), request.diaSemana());
        cacheInvalidationService.evictMenuCacheByDay(comedor.getId(), request.diaSemana());

        // 11. Log
        DiaSemana dia = DiaSemana.fromNumero(request.diaSemana());
        log.info("Horario actualizado - ID: {}, Comedor: {}, Día: {}, Tipo: {}, Horario: {}-{}",
                horario.getId(),
                comedor.getNombre(),
                dia != null ? dia.getNombre() : request.diaSemana(),
                tipo.getNombre(),
                request.horaInicio(),
                request.horaFin());

        return convertirAResponse(horario);
    }

    /**
     * 🗑️ Eliminar horario
     */
    @Transactional
    public void eliminar(Long id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("HOR_001", "Horario no encontrado", HttpStatus.NOT_FOUND));

        Long comedorId = horario.getComedor().getId();
        Integer diaSemana = horario.getDiaSemana();

        horarioRepository.delete(horario);

        // Invalidar cache
        cacheInvalidationService.evictHorariosCacheByDay(comedorId, diaSemana);
        cacheInvalidationService.evictMenuCacheByDay(comedorId, diaSemana);

        DiaSemana dia = DiaSemana.fromNumero(diaSemana);
        log.info("Horario eliminado - ID: {}, Comedor: {}, Día: {}, Tipo: {}",
                horario.getId(),
                horario.getComedor().getNombre(),
                dia != null ? dia.getNombre() : diaSemana,
                horario.getTipoConsumo().getNombre());
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    /**
     * 📋 Copiar horarios de un día a otros
     */
    @Transactional
    public void copiarHorarios(CopiarHorariosRequest request) {
        // Validar que el comedor exista
        comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        // Validar días destino
        for (Integer dia : request.diasDestino()) {
            if (dia < 1 || dia > 7) {
                throw new BusinessException("HOR_007", "Día de semana inválido: " + dia, HttpStatus.BAD_REQUEST);
            }
        }

        List<Horario> horariosOrigen = horarioRepository.findAll().stream()
                .filter(h -> h.getComedor().getId().equals(request.comedorId()))
                .filter(h -> h.getDiaSemana().equals(request.diaOrigen()))
                .toList();

        if (horariosOrigen.isEmpty()) {
            throw new BusinessException("HOR_006", "No hay horarios configurados para el día origen", HttpStatus.NOT_FOUND);
        }

        for (Integer diaDestino : request.diasDestino()) {
            // Eliminar horarios existentes
            horarioRepository.findAll().stream()
                    .filter(h -> h.getComedor().getId().equals(request.comedorId()))
                    .filter(h -> h.getDiaSemana().equals(diaDestino))
                    .forEach(horarioRepository::delete);

            // Crear nuevos horarios
            for (Horario origen : horariosOrigen) {
                Horario nuevo = Horario.builder()
                        .comedor(origen.getComedor())
                        .tipoConsumo(origen.getTipoConsumo())
                        .diaSemana(diaDestino)
                        .horaInicio(origen.getHoraInicio())
                        .horaFin(origen.getHoraFin())
                        .activo(origen.isActivo())
                        .build();
                horarioRepository.save(nuevo);
            }

            // Invalidar cache del día destino
            cacheInvalidationService.evictHorariosCacheByDay(request.comedorId(), diaDestino);
            cacheInvalidationService.evictMenuCacheByDay(request.comedorId(), diaDestino);
        }

        DiaSemana origenDia = DiaSemana.fromNumero(request.diaOrigen());
        log.info("Horarios copiados - Comedor: {}, Desde: {}, Hacia: {}",
                request.comedorId(),
                origenDia != null ? origenDia.getNombre() : request.diaOrigen(),
                request.diasDestino().stream()
                        .map(d -> {
                            DiaSemana dia = DiaSemana.fromNumero(d);
                            return dia != null ? dia.getNombre() : String.valueOf(d);
                        })
                        .collect(Collectors.joining(", ")));
    }

    // =====================================================
    // MÉTODOS PRIVADOS
    // =====================================================

    private HorarioResponse convertirAResponse(Horario horario) {
        DiaSemana dia = DiaSemana.fromNumero(horario.getDiaSemana());
        return new HorarioResponse(
                horario.getId(),
                horario.getComedor().getId(),
                horario.getComedor().getNombre(),
                horario.getTipoConsumo().getId(),
                horario.getTipoConsumo().getNombre(),
                horario.getDiaSemana(),
                dia != null ? dia.getNombre() : null,
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.isActivo()
        );
    }
}