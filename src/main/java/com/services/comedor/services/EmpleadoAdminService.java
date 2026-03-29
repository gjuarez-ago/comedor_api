package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.CrearEmpleadoRequest;
import com.services.comedor.models.CrearEmpleadoResponse;
import com.services.comedor.models.EmpleadoDTO;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.EmpleadoRepository;
import com.services.comedor.repository.TipoConsumoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("empleadoAdminLegacyService")
@RequiredArgsConstructor
public class EmpleadoAdminService {

    private final EmpleadoRepository empleadoRepository;
    private final ComedorRepository comedorRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final PasswordEncoder passwordEncoder;

    // Códigos de error
    private static final String ERROR_EMPLEADO_EXISTE = "EMP_001";
    private static final String ERROR_TELEFONO_EXISTE = "EMP_002";
    private static final String ERROR_COMEDOR_NO_ENCONTRADO = "EMP_003";
    private static final String ERROR_TIPO_CONSUMO_NO_ENCONTRADO = "EMP_004";
    private static final String ERROR_CAMIONERO_PERMISOS_INVALIDOS = "EMP_005";
    private static final String ERROR_ADMINISTRATIVO_PERMISOS_INVALIDOS = "EMP_006";

    @Transactional
    public CrearEmpleadoResponse crearEmpleado(CrearEmpleadoRequest request) {
        
        // 1. Validar número de empleado único
        if (empleadoRepository.existsByNumeroEmpleado(request.numeroEmpleado())) {
            throw new BusinessException(
                ERROR_EMPLEADO_EXISTE,
                "El número de empleado '" + request.numeroEmpleado() + "' ya existe",
                HttpStatus.CONFLICT
            );
        }
        
        // 2. Validar teléfono único
        if (empleadoRepository.existsByTelefono(request.telefono())) {
            throw new BusinessException(
                ERROR_TELEFONO_EXISTE,
                "El teléfono '" + request.telefono() + "' ya está registrado",
                HttpStatus.CONFLICT
            );
        }
        
        // 3. Validar comedor
        Comedor comedor = comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException(
                    ERROR_COMEDOR_NO_ENCONTRADO,
                    "Comedor no encontrado con ID: " + request.comedorId(),
                    HttpStatus.NOT_FOUND
                ));
        
        // 4. Validar tipos de consumo permitidos
        Set<TipoConsumo> tiposPermitidos = validarTiposConsumo(request.tiposConsumoPermitidosIds());
        
        // 5. Validar reglas de negocio según perfil
        // validarReglasPorPerfil(request.horarioFlexible(), tiposPermitidos);
        
        // 6. Crear empleado
        Empleado empleado = Empleado.builder()
                .numeroEmpleado(request.numeroEmpleado())
                .nombre(request.nombre())
                .telefono(request.telefono())
                .pin(passwordEncoder.encode(request.pin()))
                .comedor(comedor)
                .activo(request.activo() != null ? request.activo() : true)
                .consumosPermitidos(tiposPermitidos)
                .build();
        
        empleado = empleadoRepository.save(empleado);
        
        // 7. Construir respuesta
        return new CrearEmpleadoResponse(
                empleado.getId(),
                empleado.getNumeroEmpleado(),
                empleado.getNombre(),
                empleado.getTelefono(),
                empleado.getComedor().getNombre(),
                request.horarioFlexible(),
                empleado.getConsumosPermitidos().stream()
                        .map(TipoConsumo::getNombre)
                        .collect(Collectors.toList()),
                empleado.getActivo(),
                "Empleado creado exitosamente",
                "PIN asignado: " + request.pin(),
                "/api/empleado/qr/" + empleado.getId()
        );
    }
    
    private Set<TipoConsumo> validarTiposConsumo(Set<Long> tiposIds) {
        Set<TipoConsumo> tipos = new HashSet<>();
        for (Long tipoId : tiposIds) {
            TipoConsumo tipo = tipoConsumoRepository.findById(tipoId)
                    .orElseThrow(() -> new BusinessException(
                        ERROR_TIPO_CONSUMO_NO_ENCONTRADO,
                        "Tipo de consumo no encontrado con ID: " + tipoId,
                        HttpStatus.NOT_FOUND
                    ));
            tipos.add(tipo);
        }
        return tipos;
    }
    
    private void validarReglasPorPerfil(Boolean horarioFlexible, Set<TipoConsumo> tiposPermitidos) {
        if (horarioFlexible) {

            // Camioneros: Deben tener COMIDA (2) y TIENDA (99)
            boolean tieneComida = tiposPermitidos.stream().anyMatch(t -> t.getId() == 2L);
            boolean tieneTienda = tiposPermitidos.stream().anyMatch(t -> t.getId() == 99L);
            
            if (!tieneComida || !tieneTienda) {
                throw new BusinessException(
                    ERROR_CAMIONERO_PERMISOS_INVALIDOS,
                    "Los camioneros deben tener asignados COMIDA y TIENDA como mínimo",
                    HttpStatus.BAD_REQUEST
                );
            }
            
            // No pueden tener DESAYUNO ni CENA
            boolean tieneDesayuno = tiposPermitidos.stream().anyMatch(t -> t.getId() == 1L);
            boolean tieneCena = tiposPermitidos.stream().anyMatch(t -> t.getId() == 3L);
            
            if (tieneDesayuno || tieneCena) {
                throw new BusinessException(
                    ERROR_CAMIONERO_PERMISOS_INVALIDOS,
                    "Los camioneros NO pueden tener asignados DESAYUNO ni CENA",
                    HttpStatus.BAD_REQUEST
                );
            }
        } else {
            // Administrativos: Deben tener DESAYUNO (1) y COMIDA (2)
            boolean tieneDesayuno = tiposPermitidos.stream().anyMatch(t -> t.getId() == 1L);
            boolean tieneComida = tiposPermitidos.stream().anyMatch(t -> t.getId() == 2L);
            
            if (!tieneDesayuno || !tieneComida) {
                throw new BusinessException(
                    ERROR_ADMINISTRATIVO_PERMISOS_INVALIDOS,
                    "Los administrativos deben tener asignados DESAYUNO y COMIDA como mínimo",
                    HttpStatus.BAD_REQUEST
                );
            }
        }

    }

       private static final int MAX_RESULTADOS = 10;

    /**
     * Busca empleados por término (número de empleado, nombre o teléfono)
     * 
     * @param termino Texto a buscar (ej: "CAM", "Juan", "551234")
     * @return Lista de hasta 10 empleados que coinciden
     */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> buscarEmpleados(String termino) {
        
        if (termino == null || termino.trim().isEmpty()) {
            return List.of();
        }
        
        String terminoLimpio = termino.trim();
        Pageable pageable = PageRequest.of(0, MAX_RESULTADOS);
        
        // Búsqueda unificada: número, nombre o teléfono
        List<EmpleadoDTO> resultados = empleadoRepository.searchEmpleados(terminoLimpio, pageable);
        
        
        return resultados;
    }
}
