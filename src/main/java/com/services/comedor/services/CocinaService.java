package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.ComandaKdsResponse;
import com.services.comedor.models.ConsumoResponse;
import com.services.comedor.repository.ConsumoEstadoHistorialRepository;
import com.services.comedor.repository.ConsumoRepository;
import com.services.comedor.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.services.comedor.models.DetalleKdsDTO;
import com.services.comedor.models.ModificadorKdsDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CocinaService {

    private final ConsumoRepository consumoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsumoEstadoHistorialRepository consumoEstadoHistorialRepository;

    // =====================================================
    // 1. VER PEDIDOS PENDIENTES (KDS)
    // =====================================================
    /**
     * 🍳 Obtiene pedidos para pantalla de cocina (KDS)
     *
     * La cocina ve: - Pedidos en estado PAGADO (recién validados, esperando
     * cocina) - Pedidos en estado PREPARANDO (ya tomados, en proceso)
     *
     * Ordenados por fecha de creación (los más antiguos primero)
     *
     * @param comedorId ID del comedor
     * @return Lista de pedidos para cocina
     */
    public List<ComandaKdsResponse> verPedidosPendientes(Long comedorId) {

        // Cocina ve PAGADO (pendientes) y PREPARANDO (en proceso)
        List<Consumo> consumos = consumoRepository.findConsumosParaCocina(
                comedorId,
                List.of(EstadoConsumo.PAGADO, EstadoConsumo.PREPARANDO)
        );

        return consumos.stream().map(this::convertirAComanda).collect(Collectors.toList());
    }

    // =====================================================
    // 2. MARCAR COMO PREPARANDO
    // =====================================================
    /**
     * 🍳 Cocinero toma un pedido para empezar a cocinar
     *
     * @param consumoId ID del pedido
     * @param usuarioId ID del cocinero autenticado
     * @return ConsumoResponse con estado actualizado
     */
    @Transactional
    public ConsumoResponse marcarPreparando(Long consumoId, Long usuarioId) {

        Usuario cocinero = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException(
                "USR_001",
                "Usuario no encontrado",
                HttpStatus.NOT_FOUND));

        Consumo consumo = consumoRepository.findById(consumoId)
                .orElseThrow(() -> new BusinessException(
                "CON_004",
                "Pedido no encontrado",
                HttpStatus.NOT_FOUND));

        // Solo se puede marcar PREPARANDO si está en PAGADO
        if (consumo.getEstado() != EstadoConsumo.PAGADO) {
            throw new BusinessException(
                    "CON_005",
                    "Solo se pueden preparar pedidos en estado PAGADO",
                    HttpStatus.BAD_REQUEST);
        }

        consumo.setEstado(EstadoConsumo.PREPARANDO);
        consumo = consumoRepository.save(consumo);

        registrarHistorial(consumo, EstadoConsumo.PREPARANDO, cocinero);

        log.info("Pedido marcado como PREPARANDO - Consumo: {}, Cocinero: {}",
                consumo.getId(), cocinero.getUsername());

        return new ConsumoResponse(
                consumo.getId(),
                consumo.getTokenQr(),
                consumo.getEstado().name()
        );
    }

    // =====================================================
    // 3. MARCAR COMO LISTO
    // =====================================================
    /**
     * ✅ Cocinero termina de preparar el pedido
     *
     * @param consumoId ID del pedido
     * @param usuarioId ID del cocinero autenticado
     * @return ConsumoResponse con estado actualizado
     */
    @Transactional
    public ConsumoResponse marcarListo(Long consumoId, Long usuarioId) {

        Usuario cocinero = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException(
                "USR_001",
                "Usuario no encontrado",
                HttpStatus.NOT_FOUND));

        Consumo consumo = consumoRepository.findById(consumoId)
                .orElseThrow(() -> new BusinessException(
                "CON_004",
                "Pedido no encontrado",
                HttpStatus.NOT_FOUND));

        // Solo se puede marcar LISTO si está en PREPARANDO
        if (consumo.getEstado() != EstadoConsumo.PREPARANDO) {
            throw new BusinessException(
                    "CON_006",
                    "Solo se pueden marcar como LISTO pedidos en estado PREPARANDO",
                    HttpStatus.BAD_REQUEST);
        }

        consumo.setEstado(EstadoConsumo.LISTO);
        consumo = consumoRepository.save(consumo);

        registrarHistorial(consumo, EstadoConsumo.LISTO, cocinero);

        log.info("Pedido marcado como LISTO - Consumo: {}, Cocinero: {}",
                consumo.getId(), cocinero.getUsername());

        return new ConsumoResponse(
                consumo.getId(),
                consumo.getTokenQr(),
                consumo.getEstado().name()
        );
    }

    // =====================================================
    // MÉTODOS PRIVADOS
    // =====================================================
    private ComandaKdsResponse convertirAComanda(Consumo consumo) {
        List<DetalleKdsDTO> items = consumo.getDetalles().stream()
                .map(detalle -> new DetalleKdsDTO(
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getModificadores().stream()
                        .map(mod -> new ModificadorKdsDTO(mod.getNombreOpcion())) // 🔥 Convertir a ModificadorKdsDTO
                        .collect(Collectors.toSet())
        ))
                .collect(Collectors.toList());

        return new ComandaKdsResponse(
                consumo.getId(),
                consumo.getTokenQr(),
                consumo.getFechaCreacion().format(DateTimeFormatter.ofPattern("HH:mm")),
                items
        );
    }

    private void registrarHistorial(Consumo consumo, EstadoConsumo estado, Usuario usuario) {
        try {
            ConsumoEstadoHistorial historial = ConsumoEstadoHistorial.builder()
                    .consumo(consumo)
                    .estado(estado)
                    .usuario(usuario)
                    .build();
            consumoEstadoHistorialRepository.save(historial);
        } catch (Exception e) {
            log.warn("No se pudo registrar historial: {}", e.getMessage());
        }
    }
}
