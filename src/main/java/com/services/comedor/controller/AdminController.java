package com.services.comedor.controller;


import com.services.comedor.models.CrearEmpleadoRequest;
import com.services.comedor.models.CrearEmpleadoResponse;
import com.services.comedor.models.CrearUsuarioRequest;
import com.services.comedor.models.CrearUsuarioResponse;
import com.services.comedor.services.EmpleadoAdminService;
import com.services.comedor.services.UsuarioAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class AdminController {

     private final UsuarioAdminService usuarioService;

    private final EmpleadoAdminService empleadoService;
    
    @PostMapping("/crear-empleado")
    public ResponseEntity<CrearEmpleadoResponse> crearEmpleado(@Valid @RequestBody CrearEmpleadoRequest request) {
        CrearEmpleadoResponse response = empleadoService.crearEmpleado(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/crear-usuario")
    public ResponseEntity<CrearUsuarioResponse> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        CrearUsuarioResponse response = usuarioService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}
