package com.services.comedor.services.admin;

import com.services.comedor.entity.Comedor;
import com.services.comedor.entity.ComedorProducto;
import com.services.comedor.entity.Producto;
import com.services.comedor.entity.ProductoDiaDisponible;
import com.services.comedor.entity.ProductoStock;
import com.services.comedor.entity.TipoConsumo;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.models.admin.ConfiguracionComedorDTO;
import com.services.comedor.models.admin.ConfiguracionComedorResponse;
import com.services.comedor.models.admin.CrearProductoRequest;
import com.services.comedor.models.admin.ProductoResponse;
import com.services.comedor.models.admin.TipoConsumoResponse;
import com.services.comedor.repository.ComedorProductoRepository;
import com.services.comedor.repository.ComedorRepository;
import com.services.comedor.repository.ProductoDiaDisponibleRepository;
import com.services.comedor.repository.ProductoRepository;
import com.services.comedor.repository.ProductoStockRepository;
import com.services.comedor.repository.TipoConsumoRepository;
import com.services.comedor.services.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final ComedorRepository comedorRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final ComedorProductoRepository comedorProductoRepository;
    private final ProductoDiaDisponibleRepository productoDiaDisponibleRepository;
    private final ProductoStockRepository productoStockRepository;
    private final CacheInvalidationService cacheInvalidationService;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoResponse crear(CrearProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .imagenUrl(request.imagenUrl())
                .requierePreparacion(request.requierePreparacion())
                .controlaInventario(request.controlaInventario() != null ? request.controlaInventario() : false)
                .controlaPorciones(request.controlaPorciones() != null ? request.controlaPorciones() : false)
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        producto = productoRepository.save(producto);

        if (request.configuracionComedores() != null) {
            for (ConfiguracionComedorDTO config : request.configuracionComedores()) {
                crearConfiguracionComedor(producto, config);
            }
        }

        cacheInvalidationService.evictAllMenuCache();
        log.info("Producto creado - ID: {}, Nombre: {}", producto.getId(), producto.getNombre());
        return convertirAResponse(producto);
    }

    private void crearConfiguracionComedor(Producto producto, ConfiguracionComedorDTO config) {
        Comedor comedor = comedorRepository.findById(config.comedorId())
                .orElseThrow(() -> new BusinessException("COM_001", "Comedor no encontrado", HttpStatus.NOT_FOUND));

        ComedorProducto cp = ComedorProducto.builder()
                .comedor(comedor)
                .producto(producto)
                .precioEmpleado(config.precioEmpleado() != null ? config.precioEmpleado() : BigDecimal.ZERO)
                .precioEmpresa(config.precioEmpresa() != null ? config.precioEmpresa() : BigDecimal.ZERO)
                .disponible(config.disponible() != null ? config.disponible() : true)
                .build();

        cp = comedorProductoRepository.save(cp);

        if (config.turnosDisponiblesIds() != null && !config.turnosDisponiblesIds().isEmpty()) {
            Set<TipoConsumo> turnos = config.turnosDisponiblesIds().stream()
                    .map(tipoId -> tipoConsumoRepository.findById(tipoId)
                            .orElseThrow(() -> new BusinessException("TIPO_001", "Tipo no encontrado", HttpStatus.NOT_FOUND)))
                    .collect(Collectors.toSet());
            cp.setTurnosDisponibles(turnos);
            comedorProductoRepository.save(cp);
        }

        if (config.diasDisponibles() != null && !config.diasDisponibles().isEmpty()) {
            for (Integer dia : config.diasDisponibles()) {
                ProductoDiaDisponible pdd = ProductoDiaDisponible.builder()
                        .producto(producto)
                        .comedor(comedor)
                        .diaSemana(dia)
                        .disponible(true)
                        .build();
                productoDiaDisponibleRepository.save(pdd);
            }
        }

        if (Boolean.TRUE.equals(producto.getControlaInventario()) || Boolean.TRUE.equals(producto.getControlaPorciones())) {
            ProductoStock stock = ProductoStock.builder()
                    .producto(producto)
                    .comedor(comedor)
                    .stockActual(0)
                    .build();
            productoStockRepository.save(stock);
        }
    }

    private ProductoResponse convertirAResponse(Producto producto) {
        List<ConfiguracionComedorResponse> configuraciones = new ArrayList<>();

        List<ComedorProducto> comedorProductos = comedorProductoRepository.findByProductoId(producto.getId());
        for (ComedorProducto cp : comedorProductos) {
            Set<TipoConsumoResponse> turnos = cp.getTurnosDisponibles().stream()
                    .map(t -> new TipoConsumoResponse(t.getId(), t.getNombre()))
                    .collect(Collectors.toSet());

            Set<Integer> dias = productoDiaDisponibleRepository.findByProductoIdAndComedorId(
                            producto.getId(),
                            cp.getComedor().getId()
                    ).stream()
                    .map(ProductoDiaDisponible::getDiaSemana)
                    .collect(Collectors.toSet());

            configuraciones.add(new ConfiguracionComedorResponse(
                    cp.getComedor().getId(),
                    cp.getComedor().getNombre(),
                    cp.getPrecioEmpleado(),
                    cp.getPrecioEmpresa(),
                    cp.getDisponible(),
                    turnos,
                    dias
            ));
        }

        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getImagenUrl(),
                producto.getRequierePreparacion(),
                producto.getControlaInventario(),
                producto.getControlaPorciones(),
                producto.getActivo(),
                configuraciones
        );
    }
}

