package com.services.comedor.services.admin;

import com.services.comedor.entity.Comedor;
import com.services.comedor.entity.Usuario;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.ComedorResponse;
import com.services.comedor.models.admin.CrearUsuarioRequest;
import com.services.comedor.models.admin.UsuarioResponse;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.UsuarioRepository;
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
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final ComedorRepository comedorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheInvalidationService cacheInvalidationService;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BusinessException("USR_002", "El username ya existe", HttpStatus.CONFLICT);
        }

        Comedor comedorBase = comedorRepository.findById(request.comedorBaseId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        Set<Comedor> comedoresPermitidos = new HashSet<>();
        if (request.comedoresPermitidosIds() != null && !request.comedoresPermitidosIds().isEmpty()) {
            for (Long comedorId : request.comedoresPermitidosIds()) {
                Comedor comedor = comedorRepository.findById(comedorId)
                        .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));
                comedoresPermitidos.add(comedor);
            }
        } else {
            comedoresPermitidos.add(comedorBase);
        }

        if (!comedoresPermitidos.contains(comedorBase)) {
            throw new BusinessException("USR_003", "El comedor base debe estar incluido en los comedores permitidos", HttpStatus.BAD_REQUEST);
        }

        if (request.rol() == null || !request.rol().matches("ROLE_ADMIN|ROLE_CAJERO|ROLE_COCINA|ROLE_JEFE_COMEDOR")) {
            throw new BusinessException("USR_004", "Rol invalido", HttpStatus.BAD_REQUEST);
        }

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

        cacheInvalidationService.evictAllMenuCache();
        log.info("Usuario creado - ID: {}, Username: {}, Rol: {}", usuario.getId(), usuario.getUsername(), usuario.getRol());

        return convertirAResponse(usuario);
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        List<ComedorResponse> comedores = usuario.getComedoresPermitidos().stream()
                .map(c -> new ComedorResponse(c.getId(), c.getNombre(), c.getActivo()))
                .collect(Collectors.toList());

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol(),
                usuario.getComedorBase().getId(),
                usuario.getComedorBase().getNombre(),
                comedores,
                usuario.getActivo()
        );
    }
}
