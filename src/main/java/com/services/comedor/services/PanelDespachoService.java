package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.models.*;
import com.services.comedor.repository.ConsumoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PanelDespachoService {

    private final ConsumoRepository consumoRepository;

    private static final int MINUTOS_MOSTRAR_TIEMPO = 30;

    /**
     * Obtiene el panel completo para el despachador (cajero)
     * 
     * @param comedorId ID del comedor
     * @return PanelDespachoResponse con pedidos ordenados por prioridad
     */
    @Transactional(readOnly = true)
    public PanelDespachoResponse obtenerPanelDespacho(Long comedorId) {
        
        List<Consumo> consumos = consumoRepository.findPanelDespachoOptimizado(comedorId, LocalDate.now());
        
        List<FilaDespachoDTO> prioridadAlta = new ArrayList<>();
        List<FilaDespachoDTO> enEspera = new ArrayList<>();
        
        for (Consumo consumo : consumos) {
            FilaDespachoDTO fila = convertirAFilaDespacho(consumo);
            
            if (consumo.getEstado() == EstadoConsumo.LISTO) {
                prioridadAlta.add(fila);
            } else {
                enEspera.add(fila);
            }
        }
        
        return new PanelDespachoResponse(prioridadAlta, enEspera);
    }
    
    private FilaDespachoDTO convertirAFilaDespacho(Consumo consumo) {
        
        // Calcular tiempo de espera
        long minutosEspera = Duration.between(consumo.getFechaCreacion(), LocalDateTime.now()).toMinutes();
        String tiempoEspera = minutosEspera + " min";
        
        if (minutosEspera > MINUTOS_MOSTRAR_TIEMPO) {
            tiempoEspera = "⚠️ " + minutosEspera + " min (demorado)";
        }
        
        // Convertir detalles
        List<ResumenPlatilloDTO> items = consumo.getDetalles().stream()
                .map(detalle -> new ResumenPlatilloDTO(
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getModificadores().stream()
                                .map(ConsumoDetalleModificador::getNombreOpcion)
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
        
        return new FilaDespachoDTO(
                consumo.getId(),
                consumo.getTokenQr(),
                consumo.getEmpleado().getNombre(),
                consumo.getEmpleado().getNumeroEmpleado(),
                consumo.getEstado(),
                items,
                tiempoEspera
        );
    }
}