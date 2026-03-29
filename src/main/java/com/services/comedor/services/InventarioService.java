package com.services.comedor.services;

import com.services.comedor.entity.*;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.repository.ProductoStockRepository;
import com.services.comedor.repository.InventarioMovimientoRepository;
import com.services.comedor.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioService {

    // 🔥 Umbral para invalidar cache: cuando el stock es bajo
    private static final int STOCK_MINIMO_INVALIDAR_CACHE = 10;

    private final ProductoStockRepository productoStockRepository;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final ProductoRepository productoRepository;
    private final CacheInvalidationService cacheInvalidationService;

    // =====================================================
    // FLUJO OPERATIVO (USADOS EN CAJA/EMPLEADO)
    // =====================================================

    /**
     * ✅ VALIDACIÓN RÁPIDA: Verifica stock sin cargar entidad.
     * Usado en EmpleadoService.generarPedido() y CajaService.validarQR()
     */
    public void validarStock(Long productoId, Long comedorId, Integer cantidad) {
        
        // 1. ¿Existe configuración de stock?
        boolean existeConfig = productoStockRepository.existeConfiguracionStock(productoId, comedorId);
        
        if (!existeConfig) {
            throw new BusinessException(
                    "STK_001",
                    "Producto sin stock configurado",
                    HttpStatus.NOT_FOUND);
        }
        
        // 2. ¿Hay stock suficiente?
        boolean hayStock = productoStockRepository.tieneStockSuficiente(productoId, comedorId, cantidad);
        
        if (!hayStock) {
            throw new BusinessException(
                    "STK_002",
                    "Stock insuficiente",
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * ✅ DESCONTAR STOCK: Operación atómica.
     * Usado en CajaService.validarQR() y CajaService.ventaDirecta()
     * 🔥 Solo invalida cache del día actual si el stock después del descuento es bajo
     */
    @Transactional
    public void descontarStock(Long productoId, Long comedorId, Integer cantidad, Consumo consumo) {
        
        // Obtener stock actual ANTES de descontar (para saber si es crítico)
        Integer stockActual = productoStockRepository.findStockActual(productoId, comedorId)
                .orElse(0);
        
        int updated = productoStockRepository.descontarStockAtomico(productoId, comedorId, cantidad);
        
        if (updated == 0) {
            throw new BusinessException(
                    "STK_002",
                    "Stock insuficiente al momento de descontar",
                    HttpStatus.CONFLICT);
        }
        
        Integer nuevoStock = stockActual - cantidad;
        
        // 🔥 DECISIÓN: Invalidar cache SOLO si el nuevo stock es bajo o se agotó
        if (nuevoStock <= STOCK_MINIMO_INVALIDAR_CACHE) {
            int diaSemana = LocalDate.now().getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo
            cacheInvalidationService.evictMenuCacheByDay(comedorId, diaSemana);
            log.info("Cache invalidado - Producto: {}, Comedor: {}, Nuevo stock: {} (bajo/agotado)",
                    productoId, comedorId, nuevoStock);
        } else {
            log.debug("Stock suficiente - Producto: {}, Comedor: {}, Nuevo stock: {} (cache mantenido)",
                    productoId, comedorId, nuevoStock);
        }
        
        // Registrar movimiento
        registrarMovimiento(productoId, consumo, "SALIDA", cantidad, "CONSUMO");
    }

    /**
     * ✅ DEVOLVER STOCK: Operación atómica.
     * Usado en CajaService.cancelarPedido()
     * 🔥 Solo invalida cache del día actual si el stock antes de la devolución era bajo
     */
    @Transactional
    public void devolverStock(Long productoId, Long comedorId, Integer cantidad) {
        
        // Obtener stock actual ANTES de devolver
        Integer stockActual = productoStockRepository.findStockActual(productoId, comedorId)
                .orElse(0);
        
        int updated = productoStockRepository.devolverStockAtomico(productoId, comedorId, cantidad);
        
        Integer nuevoStock = stockActual + cantidad;
        
        // 🔥 DECISIÓN: Invalidar cache si el stock ANTES era bajo (≤ umbral)
        if (stockActual <= STOCK_MINIMO_INVALIDAR_CACHE) {
            int diaSemana = LocalDate.now().getDayOfWeek().getValue();
            cacheInvalidationService.evictMenuCacheByDay(comedorId, diaSemana);
            log.info("Cache invalidado - Producto: {}, Comedor: {}, Stock anterior: {} (bajo), Nuevo stock: {}",
                    productoId, comedorId, stockActual, nuevoStock);
        } else {
            log.debug("Stock suficiente - Producto: {}, Comedor: {}, Stock anterior: {} (cache mantenido)",
                    productoId, comedorId, stockActual);
        }
        
        registrarMovimiento(productoId, null, "ENTRADA", cantidad, "CANCELACION");
    }

    // =====================================================
    // MÉTODOS PRIVADOS
    // =====================================================

    private void registrarMovimiento(Long productoId, Consumo consumo, String tipo, 
                                      Integer cantidad, String motivo) {
        try {
            Producto producto = productoRepository.findById(productoId).orElse(null);
            if (producto != null) {
                InventarioMovimiento movimiento = InventarioMovimiento.builder()
                        .producto(producto)
                        .consumo(consumo)
                        .tipo(tipo)
                        .cantidad(cantidad)
                        .motivo(motivo)
                        .build();
                inventarioMovimientoRepository.save(movimiento);
            }
        } catch (Exception e) {
            log.error("Error al registrar movimiento: {}", e.getMessage());
        }
    }
}