package com.services.comedor.controller.admin;

import com.services.comedor.models.admin.ComedorResponse;
import com.services.comedor.models.admin.CrearComedorRequest;
import com.services.comedor.services.admin.ComedorAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comedores")
@RequiredArgsConstructor
public class AdminComedorController {

    private final ComedorAdminService comedorAdminService;

    @GetMapping
    public ResponseEntity<List<ComedorResponse>> listarTodos() {
        return ResponseEntity.ok(comedorAdminService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<ComedorResponse> crear(@Valid @RequestBody CrearComedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comedorAdminService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComedorResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CrearComedorRequest request) {
        return ResponseEntity.ok(comedorAdminService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        comedorAdminService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

