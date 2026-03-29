package com.services.comedor.controller;

import com.services.comedor.models.MenuResponse;
import com.services.comedor.services.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * Obtiene el menú activo para un comedor específico según la hora y día
     * actual
     *
     * El menú se determina automáticamente según: - Hora actual (desayuno,
     * comida, cena) - Día de la semana (lunes a domingo) - Stock disponible
     * (snacks y comidas con porciones)
     *
     * URL: GET /api/menu/{comedorId}
     *
     * @param comedorId ID del comedor
     * @return MenuResponse con productos disponibles según horario y día
     *
     * @example Request GET /api/menu/1
     *
     * @example Response (Lunes 13:00) { "turnoNombre": "COMIDA", "horario":
     * "12:00 - 15:00", "productos": [ { "id": 4, "nombre": "COMIDA CORRIENTE",
     * "descripcion": "Arroz, frijoles, carne asada, ensalada", "precioBase":
     * 0.0, "imagenUrl": "https://...", "gruposModificadores": [],
     * "stockDisponible": 50, "controlaStock": true } ] }
     *
     * @example Response (Domingo 13:00 - cerrado) { "turnoNombre": "TIENDA",
     * "horario": "Horario libre", "productos": [ { "id": 13, "nombre": "GALLETA
     * EMPERADOR", "precioBase": 5.0, ... } ] }
     */
    @GetMapping("/{comedorId}")
    public ResponseEntity<MenuResponse> obtenerMenuActivo(@PathVariable Long comedorId) {
        MenuResponse response = menuService.obtenerMenuActivo(comedorId);
        return ResponseEntity.ok(response);
    }

}
