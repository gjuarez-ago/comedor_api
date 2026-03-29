package com.services.comedor.controller.admin;

import com.services.comedor.models.admin.ActualizarStockRequest;
import com.services.comedor.models.admin.StockResponse;
import com.services.comedor.services.admin.StockAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stock")
@RequiredArgsConstructor
public class AdminStockController {

    private final StockAdminService stockAdminService;

    @GetMapping
    public ResponseEntity<List<StockResponse>> listarTodos() {
        return ResponseEntity.ok(stockAdminService.listarTodos());
    }

    @PutMapping
    public ResponseEntity<StockResponse> actualizarStock(@Valid @RequestBody ActualizarStockRequest request) {
        return ResponseEntity.ok(stockAdminService.actualizarStock(request));
    }
}

