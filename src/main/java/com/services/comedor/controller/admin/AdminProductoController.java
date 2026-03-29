package com.services.comedor.controller.admin;

import com.services.comedor.models.admin.CrearProductoRequest;
import com.services.comedor.models.admin.ProductoResponse;
import com.services.comedor.services.admin.ProductoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoAdminService productoAdminService;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        return ResponseEntity.ok(productoAdminService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoAdminService.crear(request));
    }
}

