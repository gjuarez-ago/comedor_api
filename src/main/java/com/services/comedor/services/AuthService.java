package com.services.comedor.services;

import com.services.comedor.entity.Empleado;
import com.services.comedor.entity.Usuario;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.exception.ErrorCodes;
import com.services.comedor.repository.EmpleadoRepository;
import com.services.comedor.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.comedor.models.LoginRequest;
import com.services.comedor.models.LoginResponse;
import com.services.comedor.models.LoginUsuarioRequest;
import com.services.comedor.models.LoginUsuarioResponse;
import com.services.comedor.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // Asumiendo que tienes una clase para generar JWT

    /**
     * 📱 Login para la App del Empleado (El que va a comer)
     */
    @Transactional(readOnly = true)
    public LoginResponse loginEmpleado(LoginRequest request) {
        
        Empleado empleado = empleadoRepository.findByTelefono(request.telefono())
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.AUTH_NOT_FOUND, 
                        ErrorCodes.MSG_AUTH_NOT_FOUND, 
                        HttpStatus.NOT_FOUND));

        if (!empleado.getActivo()) {
            throw new BusinessException(
                    ErrorCodes.AUTH_SUSPENDED, 
                    ErrorCodes.MSG_AUTH_SUSPENDED, 
                    HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.pin(), empleado.getPin())) {
            throw new BusinessException(
                    ErrorCodes.AUTH_INVALID_PIN, 
                    ErrorCodes.MSG_AUTH_INVALID_PIN, 
                    HttpStatus.UNAUTHORIZED);
        }

        // Generamos el token JWT con los claims necesarios
        String token = jwtTokenProvider.generateTokenEmpleado(empleado);

        return new LoginResponse(
                empleado.getId(),
                empleado.getNombre(),
                "ROLE_EMPLEADO",
                empleado.getComedor().getId(),
                token
        );
    }

/**
     * 💻 Login para las Tablets del Sistema (Cajeros, Cocineros, Jefes)
     */
    @Transactional(readOnly = true)
    public LoginUsuarioResponse loginStaff(LoginUsuarioRequest request) {
        
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.AUTH_NOT_FOUND, 
                        ErrorCodes.MSG_AUTH_NOT_FOUND, 
                        HttpStatus.NOT_FOUND));

        if (!usuario.getActivo()) {
            throw new BusinessException(
                    ErrorCodes.AUTH_SUSPENDED, 
                    ErrorCodes.MSG_AUTH_SUSPENDED, 
                    HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.pin(), usuario.getPin())) {
            throw new BusinessException(
                    ErrorCodes.AUTH_INVALID_PIN, 
                    ErrorCodes.MSG_AUTH_INVALID_PIN, 
                    HttpStatus.UNAUTHORIZED);
        }

        // 🛡️ REGLA DE NEGOCIO OPTIMIZADA: 
        // Si es admin, pasa directo. Si no, le preguntamos a la BD de forma ligera.
        boolean tieneAccesoAlComedor = usuario.getRol().equals("ROLE_ADMIN") 
            || usuarioRepository.tieneAccesoAComedor(usuario.getId(), request.comedorTabletId());

        if (!tieneAccesoAlComedor) {
            throw new BusinessException(
                    ErrorCodes.AUTH_UNAUTHORIZED_LOCATION, 
                    ErrorCodes.MSG_AUTH_UNAUTHORIZED_LOCATION, 
                    HttpStatus.FORBIDDEN);
        }

        // Generamos el token JWT
        String token = jwtTokenProvider.generateTokenStaff(usuario, request.comedorTabletId());

        return new LoginUsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getRol(),
                token
        );
    }   
}