package com.services.comedor.controller;

import com.services.comedor.models.LoginRequest;
import com.services.comedor.models.LoginResponse;
import com.services.comedor.models.LoginUsuarioRequest;
import com.services.comedor.models.LoginUsuarioResponse;
import com.services.comedor.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación para el sistema de comedor
 * 
 * Maneja dos tipos de login:
 * - Empleados (comensales) que usan la app móvil
 * - Staff (cajeros, cocineros, administradores) que usan tablets
 * 
 * @author TuNombre
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =====================================================
    // 🔐 LOGIN PARA EMPLEADOS (APP MÓVIL)
    // =====================================================

    /**
     * 🔐 Login para empleados (comensales) que usan la app móvil
     * 
     * Los empleados se autentican con su número de empleado (gafete) y PIN personal.
     * Este login es utilizado por la aplicación móvil del empleado.
     * 
     * URL: POST /api/auth/empleado/login
     * 
     * @param request Credenciales del empleado (número de empleado y PIN)
     * @return LoginResponse con:
     *         - id: ID interno del empleado
     *         - nombre: Nombre completo del empleado
     *         - rol: Siempre "ROLE_EMPLEADO"
     *         - comedorId: ID del comedor base del empleado
     *         - token: JWT para autenticación en futuras peticiones
     * 
     * @example Request
     *   POST /api/auth/empleado/login
     *   Content-Type: application/json
     *   {
     *     "numeroEmpleado": "CAM001",
     *     "pin": "1234"
     *   }
     * 
     * @example Response (éxito)
     *   {
     *     "id": 1,
     *     "nombre": "Pedro González",
     *     "rol": "ROLE_EMPLEADO",
     *     "comedorId": 1,
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *   }
     * 
     * @example Response (error - empleado no encontrado)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 404,
     *     "error": "Not Found",
     *     "code": "EMP_001",
     *     "message": "Empleado no encontrado",
     *     "path": "/api/auth/empleado/login"
     *   }
     * 
     * @example Response (error - PIN incorrecto)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 401,
     *     "error": "Unauthorized",
     *     "code": "AUTH_001",
     *     "message": "PIN incorrecto",
     *     "path": "/api/auth/empleado/login"
     *   }
     * 
     * @example Response (error - empleado inactivo)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 403,
     *     "error": "Forbidden",
     *     "code": "EMP_002",
     *     "message": "Empleado inactivo",
     *     "path": "/api/auth/empleado/login"
     *   }
     * 
     * @throws BusinessException con códigos:
     *         - EMP_001: Empleado no encontrado
     *         - EMP_002: Empleado inactivo
     *         - AUTH_001: PIN incorrecto
     */
    @PostMapping("/empleado/login")
    public ResponseEntity<LoginResponse> loginEmpleado(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginEmpleado(request);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 🔐 LOGIN PARA STAFF (TABLETS)
    // =====================================================

    /**
     * 🔐 Login para personal operativo (cajeros, cocineros, administradores)
     * 
     * El staff se autentica con username y PIN. Además, deben seleccionar el comedor
     * donde se encuentra la tablet físicamente. Esto permite que un mismo usuario
     * pueda operar en diferentes comedores según su turno.
     * 
     * URL: POST /api/auth/staff/login
     * 
     * @param request Credenciales del staff (username, PIN y comedor de la tablet)
     * @return LoginUsuarioResponse con:
     *         - usuarioId: ID interno del usuario
     *         - nombreCompleto: Nombre completo del usuario
     *         - rol: ROLE_CAJERO, ROLE_COCINA, ROLE_JEFE_COMEDOR o ROLE_ADMIN
     *         - tokenJwt: JWT para autenticación en futuras peticiones
     * 
     * @example Request (Cajero)
     *   POST /api/auth/staff/login
     *   Content-Type: application/json
     *   {
     *     "username": "cajero_norte",
     *     "pin": "1234",
     *     "comedorTabletId": 1
     *   }
     * 
     * @example Request (Cocinero)
     *   POST /api/auth/staff/login
     *   {
     *     "username": "cocina_sur",
     *     "pin": "1234",
     *     "comedorTabletId": 2
     *   }
     * 
     * @example Request (Jefe de comedor)
     *   POST /api/auth/staff/login
     *   {
     *     "username": "jefe_central",
     *     "pin": "1234",
     *     "comedorTabletId": 3
     *   }
     * 
     * @example Request (Administrador)
     *   POST /api/auth/staff/login
     *   {
     *     "username": "admin",
     *     "pin": "1234",
     *     "comedorTabletId": 1
     *   }
     * 
     * @example Response (éxito)
     *   {
     *     "usuarioId": 1,
     *     "nombreCompleto": "Carlos López",
     *     "rol": "ROLE_CAJERO",
     *     "tokenJwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *   }
     * 
     * @example Response (error - usuario no encontrado)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 404,
     *     "error": "Not Found",
     *     "code": "USR_001",
     *     "message": "Usuario no encontrado",
     *     "path": "/api/auth/staff/login"
     *   }
     * 
     * @example Response (error - PIN incorrecto)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 401,
     *     "error": "Unauthorized",
     *     "code": "AUTH_001",
     *     "message": "PIN incorrecto",
     *     "path": "/api/auth/staff/login"
     *   }
     * 
     * @example Response (error - usuario inactivo)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 403,
     *     "error": "Forbidden",
     *     "code": "USR_002",
     *     "message": "Usuario inactivo",
     *     "path": "/api/auth/staff/login"
     *   }
     * 
     * @example Response (error - sin acceso al comedor)
     *   {
     *     "timestamp": "2026-03-28T13:30:00",
     *     "status": 403,
     *     "error": "Forbidden",
     *     "code": "USR_003",
     *     "message": "No tienes acceso a este comedor",
     *     "path": "/api/auth/staff/login"
     *   }
     * 
     * @throws BusinessException con códigos:
     *         - USR_001: Usuario no encontrado
     *         - USR_002: Usuario inactivo
     *         - USR_003: Usuario sin acceso al comedor
     *         - AUTH_001: PIN incorrecto
     */
    @PostMapping("/staff/login")
    public ResponseEntity<LoginUsuarioResponse> loginStaff(@Valid @RequestBody LoginUsuarioRequest request) {
        LoginUsuarioResponse response = authService.loginStaff(request);
        return ResponseEntity.ok(response);
    }
}