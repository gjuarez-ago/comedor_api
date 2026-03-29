package com.services.comedor.services.admin;

import com.services.comedor.entity.TipoConsumo;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.CrearTipoConsumoRequest;
import com.services.comedor.models.admin.TipoConsumoResponse;
import com.services.comedor.repository.TipoConsumoRepository;
import com.services.comedor.services.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TipoConsumoAdminService {

    private final TipoConsumoRepository tipoConsumoRepository;
    private final CacheInvalidationService cacheInvalidationService;

    private static final Long TIPO_DESAYUNO = 1L;
    private static final Long TIPO_COMIDA = 2L;
    private static final Long TIPO_CENA = 3L;
    private static final Long TIPO_TIENDA = 99L;

    private static final Set<Long> TIPOS_PROTEGIDOS = Set.of(TIPO_DESAYUNO, TIPO_COMIDA, TIPO_CENA, TIPO_TIENDA);

    @Transactional(readOnly = true)
    public List<TipoConsumoResponse> listarTodos() {
        return tipoConsumoRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TipoConsumoResponse> listarParaHorarios() {
        return tipoConsumoRepository.findAll().stream()
                .filter(t -> !TIPO_TIENDA.equals(t.getId()))
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoConsumoResponse buscarPorId(Long id) {
        TipoConsumo tipo = tipoConsumoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND));
        return convertirAResponse(tipo);
    }

    @Transactional
    public TipoConsumoResponse crear(CrearTipoConsumoRequest request) {
        String nombre = request.nombre() != null ? request.nombre().trim().toUpperCase() : null;
        if (nombre == null || nombre.isEmpty()) {
            throw new BusinessException("TIPO_005", "Nombre invalido", HttpStatus.BAD_REQUEST);
        }

        boolean existe = tipoConsumoRepository.findByNombre(nombre).isPresent();
        if (existe) {
            throw new BusinessException("TIPO_002", "Ya existe un tipo con ese nombre", HttpStatus.CONFLICT);
        }

        TipoConsumo tipo = TipoConsumo.builder().nombre(nombre).build();
        tipo = tipoConsumoRepository.save(tipo);

        cacheInvalidationService.evictAllMenuCache();
        cacheInvalidationService.evictAllHorariosCache();

        log.info("Tipo de consumo creado - ID: {}, Nombre: {}", tipo.getId(), tipo.getNombre());
        return convertirAResponse(tipo);
    }

    @Transactional
    public TipoConsumoResponse actualizar(Long id, CrearTipoConsumoRequest request) {
        if (TIPOS_PROTEGIDOS.contains(id)) {
            throw new BusinessException("TIPO_003", "No se puede modificar un tipo base del sistema", HttpStatus.FORBIDDEN);
        }

        TipoConsumo tipo = tipoConsumoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND));

        String nombre = request.nombre() != null ? request.nombre().trim().toUpperCase() : null;
        if (nombre == null || nombre.isEmpty()) {
            throw new BusinessException("TIPO_005", "Nombre invalido", HttpStatus.BAD_REQUEST);
        }

        boolean existe = tipoConsumoRepository.findByNombre(nombre).isPresent()
                && !tipo.getNombre().equalsIgnoreCase(nombre);
        if (existe) {
            throw new BusinessException("TIPO_002", "Ya existe un tipo con ese nombre", HttpStatus.CONFLICT);
        }

        tipo.setNombre(nombre);
        tipo = tipoConsumoRepository.save(tipo);

        cacheInvalidationService.evictAllMenuCache();
        cacheInvalidationService.evictAllHorariosCache();

        log.info("Tipo de consumo actualizado - ID: {}, Nombre: {}", tipo.getId(), tipo.getNombre());
        return convertirAResponse(tipo);
    }

    @Transactional
    public void eliminar(Long id) {
        if (TIPOS_PROTEGIDOS.contains(id)) {
            throw new BusinessException("TIPO_004", "No se puede eliminar un tipo base del sistema", HttpStatus.FORBIDDEN);
        }

        TipoConsumo tipo = tipoConsumoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND));

        tipoConsumoRepository.delete(tipo);

        cacheInvalidationService.evictAllMenuCache();
        cacheInvalidationService.evictAllHorariosCache();

        log.info("Tipo de consumo eliminado - ID: {}, Nombre: {}", tipo.getId(), tipo.getNombre());
    }

    private TipoConsumoResponse convertirAResponse(TipoConsumo tipo) {
        return new TipoConsumoResponse(tipo.getId(), tipo.getNombre());
    }
}
