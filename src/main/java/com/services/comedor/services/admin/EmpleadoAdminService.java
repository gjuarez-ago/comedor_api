package com.services.comedor.services.admin;

import com.services.comedor.entity.Comedor;
import com.services.comedor.entity.Empleado;
import com.services.comedor.entity.TipoConsumo;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.CrearEmpleadoRequest;
import com.services.comedor.models.admin.EmpleadoResponse;
import com.services.comedor.models.admin.TipoConsumoResponse;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.EmpleadoRepository;
import com.services.comedor.repository.TipoConsumoRepository;
import com.services.comedor.services.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpleadoAdminService {

    private final EmpleadoRepository empleadoRepository;
    private final ComedorRepository comedorRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheInvalidationService cacheInvalidationService;

    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listarTodos() {
        return empleadoRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmpleadoResponse crear(CrearEmpleadoRequest request) {
        if (empleadoRepository.existsByNumeroEmpleado(request.numeroEmpleado())) {
            throw new BusinessException("EMP_002", "El numero de empleado ya existe", HttpStatus.CONFLICT);
        }

        if (empleadoRepository.existsByTelefono(request.telefono())) {
            throw new BusinessException("EMP_003", "El telefono ya esta registrado", HttpStatus.CONFLICT);
        }

        Comedor comedor = comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        Set<TipoConsumo> tiposPermitidos = new HashSet<>();
        if (request.tiposConsumoPermitidosIds() != null) {
            for (Long tipoId : request.tiposConsumoPermitidosIds()) {
                TipoConsumo tipo = tipoConsumoRepository.findById(tipoId)
                        .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND));
                tiposPermitidos.add(tipo);
            }
        }

        Empleado empleado = Empleado.builder()
                .numeroEmpleado(request.numeroEmpleado())
                .nombre(request.nombre())
                .telefono(request.telefono())
                .pin(passwordEncoder.encode(request.pin()))
                .comedor(comedor)
                .horarioFlexible(request.horarioFlexible())
                .activo(request.activo() != null ? request.activo() : true)
                .consumosPermitidos(tiposPermitidos)
                .build();

        empleado = empleadoRepository.save(empleado);

        cacheInvalidationService.evictAllMenuCache();
        log.info("Empleado creado - ID: {}, Numero: {}, Nombre: {}", empleado.getId(), empleado.getNumeroEmpleado(), empleado.getNombre());

        return convertirAResponse(empleado);
    }

    private EmpleadoResponse convertirAResponse(Empleado empleado) {
        Set<TipoConsumoResponse> permisos = empleado.getConsumosPermitidos().stream()
                .map(t -> new TipoConsumoResponse(t.getId(), t.getNombre()))
                .collect(Collectors.toSet());

        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getNumeroEmpleado(),
                empleado.getNombre(),
                empleado.getTelefono(),
                empleado.getComedor().getId(),
                empleado.getComedor().getNombre(),
                empleado.getHorarioFlexible(),
                permisos,
                empleado.getActivo()
        );
    }
}
