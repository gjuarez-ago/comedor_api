package com.services.comedor.controller.admin;

import com.services.comedor.models.admin.CopiarHorariosRequest;
import com.services.comedor.models.admin.CrearHorarioRequest;
import com.services.comedor.models.admin.HorarioResponse;
import com.services.comedor.services.admin.HorarioAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🕐 CONTROLADOR: Administración de Horarios
 * 
 * Endpoints para gestionar horarios de servicio por comedor, día y tipo de consumo
 */
@RestController
@RequestMapping("/api/admin/horarios")
@RequiredArgsConstructor
public class AdminHorarioController {

    private final HorarioAdminService horarioAdminService;

    // =====================================================
    // CRUD BÁSICO
    // =====================================================

    /**
     * 📋 Listar todos los horarios
     * GET /api/admin/horarios
     */
    @GetMapping
    public ResponseEntity<List<HorarioResponse>> listarTodos() {
        return ResponseEntity.ok(horarioAdminService.listarTodos());
    }

    /**
     * 📋 Listar horarios por comedor
     * GET /api/admin/horarios/comedor/{comedorId}
     */
    @GetMapping("/comedor/{comedorId}")
    public ResponseEntity<List<HorarioResponse>> listarPorComedor(@PathVariable Long comedorId) {
        return ResponseEntity.ok(horarioAdminService.listarPorComedor(comedorId));
    }

    /**
     * 📋 Listar horarios por comedor y día
     * GET /api/admin/horarios/comedor/{comedorId}/dia/{diaSemana}
     */
    @GetMapping("/comedor/{comedorId}/dia/{diaSemana}")
    public ResponseEntity<List<HorarioResponse>> listarPorComedorYDia(
            @PathVariable Long comedorId,
            @PathVariable Integer diaSemana) {
        return ResponseEntity.ok(horarioAdminService.listarPorComedorYDia(comedorId, diaSemana));
    }

    /**
     * 🔍 Buscar horario por ID
     * GET /api/admin/horarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HorarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(horarioAdminService.buscarPorId(id));
    }

    /**
     * ✨ Crear nuevo horario
     * POST /api/admin/horarios
     */
    @PostMapping
    public ResponseEntity<HorarioResponse> crear(@Valid @RequestBody CrearHorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioAdminService.crear(request));
    }

    /**
     * ✏️ Actualizar horario
     * PUT /api/admin/horarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<HorarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CrearHorarioRequest request) {
        return ResponseEntity.ok(horarioAdminService.actualizar(id, request));
    }

    /**
     * 🗑️ Eliminar horario
     * DELETE /api/admin/horarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        horarioAdminService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 📋 Copiar horarios de un día a otros
     * POST /api/admin/horarios/copiar
     */
    @PostMapping("/copiar")
    public ResponseEntity<Void> copiar(@Valid @RequestBody CopiarHorariosRequest request) {
        horarioAdminService.copiarHorarios(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}