package com.services.comedor.controller.admin;

import com.services.comedor.models.admin.TipoConsumoResponse;
import com.services.comedor.services.admin.TipoConsumoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tipos-consumo")
@RequiredArgsConstructor
public class AdminTipoConsumoController {

    private final TipoConsumoAdminService tipoConsumoAdminService;

    @GetMapping
    public ResponseEntity<List<TipoConsumoResponse>> listarTodos() {
        return ResponseEntity.ok(tipoConsumoAdminService.listarTodos());
    }
}

