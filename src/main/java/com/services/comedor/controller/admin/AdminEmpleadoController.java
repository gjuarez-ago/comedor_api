package com.services.comedor.controller.admin;

import com.services.comedor.models.admin.CrearEmpleadoRequest;
import com.services.comedor.models.admin.EmpleadoResponse;
import com.services.comedor.services.admin.EmpleadoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/empleados")
@RequiredArgsConstructor
public class AdminEmpleadoController {

    private final EmpleadoAdminService empleadoAdminService;

    @GetMapping
    public ResponseEntity<List<EmpleadoResponse>> listarTodos() {
        return ResponseEntity.ok(empleadoAdminService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody CrearEmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoAdminService.crear(request));
    }
}

