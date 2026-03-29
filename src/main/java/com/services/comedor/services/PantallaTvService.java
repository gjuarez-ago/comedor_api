package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.models.*;
import com.services.comedor.repository.ConsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PantallaTvService {

    private final ConsumoRepository consumoRepository;

    /**
     * Obtiene los pedidos para pantalla TV pública
     *
     * @param comedorId ID del comedor
     * @return PantallaTvResponse con pedidos LISTO
     */
    @Transactional(readOnly = true)
    public PantallaTvResponse obtenerPantallaTv(Long comedorId) {

        // Obtener pedidos LISTO (listos para recoger)
        List<Consumo> listos = consumoRepository.findByComedorIdAndEstado(comedorId, EstadoConsumo.LISTO);

        // Obtener pedidos PREPARANDO (para mostrar contador opcional)
        List<Consumo> enPreparacion = consumoRepository.findByComedorIdAndEstado(comedorId, EstadoConsumo.PREPARANDO);

        List<TicketTvDTO> listosDTO = listos.stream()
                .map(c -> new TicketTvDTO(c.getTokenQr(), "LISTO"))
                .collect(Collectors.toList());

        List<TicketTvDTO> enPreparacionDTO = enPreparacion.stream()
                .map(c -> new TicketTvDTO(c.getTokenQr(), "PREPARANDO"))
                .collect(Collectors.toList());

        return new PantallaTvResponse(enPreparacionDTO, listosDTO);
    }

    /**
     * Obtiene solo los pedidos listos (versión simplificada para TV)
     */
    @Transactional(readOnly = true)
    public List<TicketTvDTO> obtenerPedidosListos(Long comedorId) {
        List<Consumo> listos = consumoRepository.findByComedorIdAndEstado(comedorId, EstadoConsumo.LISTO);

        return listos.stream()
                .map(c -> new TicketTvDTO(c.getTokenQr(), "LISTO"))
                .collect(Collectors.toList());
    }
}
