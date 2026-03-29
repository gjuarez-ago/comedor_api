package com.services.comedor.services.admin;

import com.services.comedor.entity.Comedor;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.ComedorResponse;
import com.services.comedor.models.admin.CrearComedorRequest;
import com.services.comedor.repository.ComedorRepository;
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
public class ComedorAdminService {

    private final ComedorRepository comedorRepository;
    private final CacheInvalidationService cacheInvalidationService;

    @Transactional(readOnly = true)
    public List<ComedorResponse> listarTodos() {
        return comedorRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComedorResponse> listarActivos() {
        return comedorRepository.findByActivoTrue().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ComedorResponse buscarPorId(Long id) {
        Comedor comedor = comedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));
        return convertirAResponse(comedor);
    }

    @Transactional
    public ComedorResponse crear(CrearComedorRequest request) {
        boolean existe = comedorRepository.findAll().stream()
                .anyMatch(c -> c.getNombre() != null && c.getNombre().equalsIgnoreCase(request.nombre()));

        if (existe) {
            throw new BusinessException("COM_002", "Ya existe un comedor con ese nombre", HttpStatus.CONFLICT);
        }

        Comedor comedor = Comedor.builder()
                .nombre(request.nombre())
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        comedor = comedorRepository.save(comedor);
        cacheInvalidationService.evictAllMenuCache();

        log.info("Comedor creado - ID: {}, Nombre: {}", comedor.getId(), comedor.getNombre());

        return convertirAResponse(comedor);
    }

    @Transactional
    public ComedorResponse actualizar(Long id, CrearComedorRequest request) {
        Comedor comedor = comedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        boolean existe = comedorRepository.findAll().stream()
                .anyMatch(c -> c.getId() != null
                        && !c.getId().equals(id)
                        && c.getNombre() != null
                        && c.getNombre().equalsIgnoreCase(request.nombre()));

        if (existe) {
            throw new BusinessException("COM_002", "Ya existe un comedor con ese nombre", HttpStatus.CONFLICT);
        }

        comedor.setNombre(request.nombre());
        comedor.setActivo(request.activo() != null ? request.activo() : comedor.getActivo());

        comedor = comedorRepository.save(comedor);
        cacheInvalidationService.evictAllMenuCache();

        log.info("Comedor actualizado - ID: {}, Nombre: {}", comedor.getId(), comedor.getNombre());

        return convertirAResponse(comedor);
    }

    @Transactional
    public void eliminar(Long id) {
        Comedor comedor = comedorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        comedor.setActivo(false);
        comedorRepository.save(comedor);
        cacheInvalidationService.evictAllMenuCache();

        log.info("Comedor desactivado - ID: {}, Nombre: {}", comedor.getId(), comedor.getNombre());
    }

    private ComedorResponse convertirAResponse(Comedor comedor) {
        return new ComedorResponse(comedor.getId(), comedor.getNombre(), comedor.getActivo());
    }
}
