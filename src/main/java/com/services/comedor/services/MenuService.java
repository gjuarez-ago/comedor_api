package com.services.comedor.services;

import com.services.comedor.entity.Horario;
import com.services.comedor.exception.BusinessException;
import com.services.comedor.exception.ErrorCodes;
import com.services.comedor.models.*;
import com.services.comedor.repository.ComedorProductoRepository;
import com.services.comedor.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final HorarioRepository horarioRepository;
    private final ComedorProductoRepository comedorProductoRepository;

    private static final Long TIPO_TIENDA_ID = 99L;

    @Transactional(readOnly = true)
    @Cacheable(value = "menu", key = "#comedorId + '_' + #diaSemana")  // 🔥 CACHE por día
    public MenuResponse obtenerMenuActivo(Long comedorId) {
        LocalTime ahora = LocalTime.now();
        int diaSemana = LocalDate.now().getDayOfWeek().getValue(); // 1=Lunes, 2=Martes, ..., 7=Domingo

        // 1. Buscar horario activo por día y hora
        Horario horarioActivo = horarioRepository.findActiveByDayAndTime(comedorId, diaSemana, ahora)
                .orElse(null);
        
        Long tipoActivoId = horarioActivo != null ? horarioActivo.getTipoConsumo().getId() : null;

        // 2. UNA SOLA CONSULTA NATIVA con filtro por día
        List<Object[]> resultados = comedorProductoRepository.findMenuRawByDay(comedorId, diaSemana);

        // 3. Mapear y filtrar en memoria
        List<ProductoDTO> productos = new ArrayList<>();

        for (Object[] row : resultados) {
            // Extraer valores del arreglo
            int index = 0;
            Long comedorProductoId = ((Number) row[index++]).longValue();
            Long productoId = ((Number) row[index++]).longValue();
            String nombre = (String) row[index++];
            String descripcion = (String) row[index++];
            String imagenUrl = (String) row[index++];
            BigDecimal precioEmpleado = (BigDecimal) row[index++];
            Boolean controlaInventario = (Boolean) row[index++];
            Integer stockActual = row[index] != null ? ((Number) row[index++]).intValue() : 0;
            Long tipoConsumoId = ((Number) row[index++]).longValue();

            // Determinar si debe mostrarse en el menú actual
            boolean esTipoActivo = tipoActivoId != null && tipoConsumoId.equals(tipoActivoId);
            boolean esTienda = tipoConsumoId.equals(TIPO_TIENDA_ID);

            if (esTipoActivo) {
                // Producto de comida (desayuno, comida o cena)
                productos.add(convertirAProductoDTO(
                        productoId, nombre, descripcion, imagenUrl, precioEmpleado
                ));
            } else if (esTienda) {
                // Snack
                productos.add(convertirAProductoSnackDTO(
                        productoId, nombre, descripcion, imagenUrl, precioEmpleado
                ));
            }
        }

        // 4. Validar que haya al menos algo para mostrar
        if (productos.isEmpty()) {
            throw new BusinessException(
                    ErrorCodes.MENU_EMPTY,
                    ErrorCodes.MSG_MENU_EMPTY,
                    HttpStatus.NO_CONTENT
            );
        }

        // 5. Determinar nombre del turno y rango de horas para la respuesta
        String turnoNombre;
        String rangoHoras;

        if (horarioActivo != null) {
            turnoNombre = horarioActivo.getTipoConsumo().getNombre();
            rangoHoras = horarioActivo.getHoraInicio() + " - " + horarioActivo.getHoraFin();
        } else {
            turnoNombre = "TIENDA";
            rangoHoras = "Horario libre";
        }

        return new MenuResponse(turnoNombre, rangoHoras, productos);
    }

    // Conversión para productos de comida (pueden tener modificadores)
    private ProductoDTO convertirAProductoDTO(
            Long id, String nombre, String descripcion, String imagenUrl, BigDecimal precio) {
        
        return new ProductoDTO(
                id,
                nombre,
                descripcion,
                precio != null ? precio.doubleValue() : 0.0,
                imagenUrl,
                Set.of() // Sin modificadores por ahora
        );
    }

    // Conversión para snacks (nunca tienen modificadores)
    private ProductoDTO convertirAProductoSnackDTO(
            Long id, String nombre, String descripcion, String imagenUrl, BigDecimal precio) {
        
        return new ProductoDTO(
                id,
                nombre,
                descripcion,
                precio != null ? precio.doubleValue() : 0.0,
                imagenUrl,
                Set.of() // Snacks sin modificadores
        );
    }
}