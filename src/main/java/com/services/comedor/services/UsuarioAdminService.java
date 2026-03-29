package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.CrearUsuarioRequest;
import com.services.comedor.models.CrearUsuarioResponse;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service("usuarioAdminLegacyService")
@RequiredArgsConstructor
public class UsuarioAdminService {
      private final UsuarioRepository usuarioRepository;
    private final ComedorRepository comedorRepository;
    private final PasswordEncoder passwordEncoder;

    // Códigos de error
    private static final String ERROR_USERNAME_EXISTE = "USR_001";
    private static final String ERROR_COMEDOR_NO_ENCONTRADO = "USR_002";
    private static final String ERROR_COMEDOR_BASE_NO_INCLUIDO = "USR_003";

    @Transactional
    public CrearUsuarioResponse crearUsuario(CrearUsuarioRequest request) {
        
        // 1. Validar que username no exista
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BusinessException(
                ERROR_USERNAME_EXISTE,
                "El username '" + request.username() + "' ya existe",
                HttpStatus.CONFLICT
            );
        }
        
        // 2. Validar que comedor base exista
        Comedor comedorBase = comedorRepository.findById(request.comedorBaseId())
                .orElseThrow(() -> new BusinessException(
                    ERROR_COMEDOR_NO_ENCONTRADO,
                    "Comedor base no encontrado con ID: " + request.comedorBaseId(),
                    HttpStatus.NOT_FOUND
                ));
        
        // 3. Validar comedores permitidos
        Set<Comedor> comedoresPermitidos = new HashSet<>();
        for (Long comedorId : request.comedoresPermitidosIds()) {
            Comedor comedor = comedorRepository.findById(comedorId)
                    .orElseThrow(() -> new BusinessException(
                        ERROR_COMEDOR_NO_ENCONTRADO,
                        "Comedor no encontrado con ID: " + comedorId,
                        HttpStatus.NOT_FOUND
                    ));
            comedoresPermitidos.add(comedor);
        }
        
        // 4. Validar que el comedor base esté incluido en los permitidos
        if (!comedoresPermitidos.contains(comedorBase)) {
            throw new BusinessException(
                ERROR_COMEDOR_BASE_NO_INCLUIDO,
                "El comedor base debe estar incluido en los comedores permitidos",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // 5. Crear usuario
        Usuario usuario = Usuario.builder()
                .username(request.username())
                .nombre(request.nombre())
                .pin(passwordEncoder.encode(request.pin()))
                .rol(request.rol())
                .comedorBase(comedorBase)
                .comedoresPermitidos(comedoresPermitidos)
                .activo(request.activo() != null ? request.activo() : true)
                .build();
        
        usuario = usuarioRepository.save(usuario);
        
        // 6. Construir respuesta
        return new CrearUsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getComedorBase().getNombre(),
                usuario.getComedoresPermitidos().stream()
                        .map(Comedor::getNombre)
                        .collect(Collectors.toList()),
                usuario.getActivo(),
                "Usuario creado exitosamente",
                "PIN asignado: " + request.pin()
        );
    }
}
