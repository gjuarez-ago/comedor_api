package com.services.comedor.controller;

import com.services.comedor.models.ComboComedorDTO;
import com.services.comedor.models.ComboDiaSemanaDTO;
import com.services.comedor.models.ComboProductoDTO;
import com.services.comedor.models.ComboTipoConsumoDTO;
import com.services.comedor.models.ConfiguracionInicialHorarioDTO;
import com.services.comedor.models.ConfiguracionInicialProductoDTO;
import com.services.comedor.services.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/comedores")
    public ResponseEntity<List<ComboComedorDTO>> getComedores() {
        return ResponseEntity.ok(catalogoService.getComedoresParaCombo());
    }

    @GetMapping("/tipos-consumo")
    public ResponseEntity<List<ComboTipoConsumoDTO>> getTiposConsumo() {
        return ResponseEntity.ok(catalogoService.getTiposConsumoParaCombo());
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ComboProductoDTO>> getProductos() {
        return ResponseEntity.ok(catalogoService.getProductosParaCombo());
    }

    @GetMapping("/dias-semana")
    public ResponseEntity<List<ComboDiaSemanaDTO>> getDiasSemana() {
        return ResponseEntity.ok(catalogoService.getDiasSemanaParaCombo());
    }

    @GetMapping("/configuracion/producto")
    public ResponseEntity<ConfiguracionInicialProductoDTO> getConfiguracionProducto() {
        return ResponseEntity.ok(catalogoService.getConfiguracionInicialProducto());
    }

    @GetMapping("/configuracion/horario")
    public ResponseEntity<ConfiguracionInicialHorarioDTO> getConfiguracionHorario() {
        return ResponseEntity.ok(catalogoService.getConfiguracionInicialHorario());
    }
}

