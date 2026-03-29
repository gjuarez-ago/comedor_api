package com.services.comedor.services.admin;

import com.services.comedor.entity.Producto;
import com.services.comedor.entity.ProductoStock;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.ActualizarStockRequest;
import com.services.comedor.models.admin.StockResponse;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.ProductoRepository;
import com.services.comedor.repository.ProductoStockRepository;
import com.services.comedor.services.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockAdminService {

    private final ProductoStockRepository productoStockRepository;
    private final ProductoRepository productoRepository;
    private final ComedorRepository comedorRepository;
    private final CacheInvalidationService cacheInvalidationService;

    @Transactional(readOnly = true)
    public List<StockResponse> listarTodos() {
        return productoStockRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockResponse actualizarStock(ActualizarStockRequest request) {
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new BusinessException("PROD_001", "Producto no encontrado", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(producto.getControlaInventario()) && !Boolean.TRUE.equals(producto.getControlaPorciones())) {
            throw new BusinessException("STK_002", "Este producto no controla stock", HttpStatus.BAD_REQUEST);
        }

        comedorRepository.findById(request.comedorId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        ProductoStock stock = productoStockRepository.findByProductoIdAndComedorId(request.productoId(), request.comedorId())
                .orElseGet(() -> {
                    ProductoStock nuevo = ProductoStock.builder()
                            .producto(producto)
                            .comedor(comedorRepository.findById(request.comedorId()).get())
                            .stockActual(0)
                            .build();
                    return productoStockRepository.save(nuevo);
                });

        int stockAnterior = stock.getStockActual();
        stock.setStockActual(request.cantidad());
        stock = productoStockRepository.save(stock);

        int diaSemana = java.time.LocalDate.now().getDayOfWeek().getValue();
        cacheInvalidationService.evictMenuCacheByDay(request.comedorId(), diaSemana);

        log.info("Stock actualizado - Producto: {}, Comedor: {}, Stock anterior: {}, Nuevo stock: {}, Motivo: {}",
                producto.getNombre(),
                request.comedorId(),
                stockAnterior,
                request.cantidad(),
                request.motivo() != null ? request.motivo() : "ACTUALIZACION_MANUAL");

        return convertirAResponse(stock);
    }

    private StockResponse convertirAResponse(ProductoStock stock) {
        boolean controlaStock = Boolean.TRUE.equals(stock.getProducto().getControlaInventario())
                || Boolean.TRUE.equals(stock.getProducto().getControlaPorciones());

        return new StockResponse(
                stock.getProducto().getId(),
                stock.getProducto().getNombre(),
                stock.getComedor().getId(),
                stock.getComedor().getNombre(),
                stock.getStockActual(),
                controlaStock
        );
    }
}

