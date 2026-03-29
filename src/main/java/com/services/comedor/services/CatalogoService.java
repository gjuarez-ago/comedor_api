package com.services.comedor.services;

import com.services.comedor.enums.DiaSemana;
import com.services.comedor.models.ComboComedorDTO;
import com.services.comedor.models.ComboDiaSemanaDTO;
import com.services.comedor.models.ComboProductoDTO;
import com.services.comedor.models.ComboTipoConsumoDTO;
import com.services.comedor.models.ConfiguracionInicialEmpleadoDTO;
import com.services.comedor.models.ConfiguracionInicialHorarioDTO;
import com.services.comedor.models.ConfiguracionInicialProductoDTO;
import com.services.comedor.models.ConfiguracionInicialUsuarioDTO;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.HorarioRepository;
import com.services.comedor.repository.ProductoRepository;
import com.services.comedor.repository.TipoConsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ComedorRepository comedorRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final ProductoRepository productoRepository;
    private final HorarioRepository horarioRepository;

    @Transactional(readOnly = true)
    public List<ComboComedorDTO> getComedoresParaCombo() {
        return comedorRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(c -> new ComboComedorDTO(c.getId(), c.getNombre(), c.getActivo()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComboTipoConsumoDTO> getTiposConsumoParaCombo() {
        return tipoConsumoRepository.findAll().stream()
                .map(t -> new ComboTipoConsumoDTO(t.getId(), t.getNombre()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComboProductoDTO> getProductosParaCombo() {
        return productoRepository.findByActivoTrue().stream()
                .map(p -> new ComboProductoDTO(p.getId(), p.getNombre(), p.getRequierePreparacion()))
                .collect(Collectors.toList());
    }

    public List<ComboDiaSemanaDTO> getDiasSemanaParaCombo() {
        return Arrays.stream(DiaSemana.values())
                .map(d -> new ComboDiaSemanaDTO(d.getNumero(), d.getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConfiguracionInicialProductoDTO getConfiguracionInicialProducto() {
        return new ConfiguracionInicialProductoDTO(
                getComedoresParaCombo(),
                getTiposConsumoParaCombo(),
                getDiasSemanaParaCombo()
        );
    }

    @Transactional(readOnly = true)
    public ConfiguracionInicialHorarioDTO getConfiguracionInicialHorario() {
        return new ConfiguracionInicialHorarioDTO(
                getComedoresParaCombo(),
                getTiposConsumoParaCombo(),
                getDiasSemanaParaCombo()
        );
    }

    @Transactional(readOnly = true)
    public ConfiguracionInicialEmpleadoDTO getConfiguracionInicialEmpleado() {
        return new ConfiguracionInicialEmpleadoDTO(
                getComedoresParaCombo(),
                getTiposConsumoParaCombo()
        );
    }

    public ConfiguracionInicialUsuarioDTO getConfiguracionInicialUsuario() {
        return new ConfiguracionInicialUsuarioDTO(
                getComedoresParaCombo(),
                List.of("ROLE_CAJERO", "ROLE_COCINA", "ROLE_JEFE_COMEDOR", "ROLE_ADMIN")
        );
    }
}

