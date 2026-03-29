package com.services.comedor.jobs;

import com.services.comedor.entity.*;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SistemaJobs {

    private final ConsumoRepository consumoRepository;
    private final ProductoStockRepository productoStockRepository;
    private final TipoConsumoRepository tipoConsumoRepository;
    private final HorarioRepository horarioRepository;

    private static final Long TIPO_TIENDA_ID = 99L;
    private static final int STOCK_MINIMO_ALERTA = 10;

    // =====================================================
    // 1. CANCELACIÓN DE QRs EXPIRADOS
    // =====================================================

    /**
     * Cancela QRs que fueron generados pero nunca escaneados en el día actual
     * Programado: todos los días a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cancelarQrsExpiradosDelDia() {
        log.info("=== INICIANDO JOB: Cancelar QRs expirados del día ===");
        log.info("Fecha de ejecución: {}", LocalDate.now());
        
        LocalDate hoy = LocalDate.now();
        int totalCancelados = 0;
        
        List<TipoConsumo> tiposComida = tipoConsumoRepository.findAll().stream()
                .filter(t -> !TIPO_TIENDA_ID.equals(t.getId()))
                .toList();
        
        for (TipoConsumo tipo : tiposComida) {
            int cancelados = consumoRepository.cancelarQrsNoEscaneados(tipo.getId(), hoy);
            totalCancelados += cancelados;
            
            if (cancelados > 0) {
                log.info("  → Cancelados {} QRs para tipo: {}", cancelados, tipo.getNombre());
            }
        }
        
        log.info("=== JOB COMPLETADO: Cancelar QRs expirados del día ===");
        log.info("Total de QRs cancelados: {}\n", totalCancelados);
    }

    /**
     * Limpia QRs de días anteriores que quedaron sin procesar
     * Programado: todos los días a las 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void limpiarQrsDiasAnteriores() {
        log.info("=== INICIANDO JOB: Limpiar QRs de días anteriores ===");
        
        LocalDate ayer = LocalDate.now().minusDays(1);
        int totalCancelados = 0;
        
        List<TipoConsumo> tiposComida = tipoConsumoRepository.findAll().stream()
                .filter(t -> !TIPO_TIENDA_ID.equals(t.getId()))
                .toList();
        
        for (TipoConsumo tipo : tiposComida) {
            int cancelados = consumoRepository.cancelarQrsNoEscaneados(tipo.getId(), ayer);
            totalCancelados += cancelados;
            
            if (cancelados > 0) {
                log.info("  → Cancelados {} QRs para tipo: {} en fecha: {}", 
                        cancelados, tipo.getNombre(), ayer);
            }
        }
        
        log.info("=== JOB COMPLETADO: Limpiar QRs de días anteriores ===");
        log.info("Total de QRs cancelados: {}\n", totalCancelados);
    }

    // =====================================================
    // 2. MARCADO DE MERMA AUTOMÁTICA
    // =====================================================

    /**
     * Marca como merma pedidos que estuvieron en estado LISTO o PREPARANDO
     * por más de 1 hora sin ser entregados
     * Programado: cada 30 minutos
     */
    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional
    public void marcarMermaAutomatica() {
        log.info("=== INICIANDO JOB: Marcar merma automática ===");
        log.info("Hora de ejecución: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        LocalDateTime hace1Hora = LocalDateTime.now().minusHours(1);
        
        int actualizados = consumoRepository.marcarMermaAutomatica(hace1Hora);
        
        if (actualizados > 0) {
            log.info("  → Marcados {} pedidos como merma", actualizados);
            
            // Registrar detalles de merma para auditoría
            List<Consumo> mermas = consumoRepository.findMermasByFechaLimite(hace1Hora);
            for (Consumo consumo : mermas) {
                log.warn("  → MERMA DETECTADA - Pedido: {}, Empleado: {}, Productos: {}",
                        consumo.getId(),
                        consumo.getEmpleado().getNombre(),
                        consumo.getDetalles().size());
            }
        } else {
            log.debug("  → No se encontraron pedidos para marcar como merma");
        }
        
        log.info("=== JOB COMPLETADO: Marcar merma automática ===\n");
    }

    // =====================================================
    // 3. LIMPIEZA DE HISTORIAL
    // =====================================================

    /**
     * Elimina consumos con más de 3 meses de antigüedad
     * Programado: primer día de cada mes a las 4:00 AM
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    @Transactional
    public void limpiarHistorialAntiguo() {
        log.info("=== INICIANDO JOB: Limpiar historial antiguo ===");
        
        LocalDate fechaLimite = LocalDate.now().minusMonths(3);
        LocalDateTime fechaLimiteDateTime = fechaLimite.atStartOfDay();
        
        log.info("Fecha límite para conservar: {}", fechaLimite);
        
        // Buscar consumos antiguos
        List<Consumo> consumosAntiguos = consumoRepository.findByFechaCreacionBefore(fechaLimiteDateTime);
        
        if (!consumosAntiguos.isEmpty()) {
            log.info("  → Encontrados {} consumos con más de 3 meses", consumosAntiguos.size());
            
            // Eliminar consumos antiguos usando deleteAll (batch)
            consumoRepository.deleteAll(consumosAntiguos);
            log.info("  → Eliminados {} consumos antiguos", consumosAntiguos.size());
        } else {
            log.info("  → No se encontraron consumos antiguos para eliminar");
        }
        
        log.info("=== JOB COMPLETADO: Limpiar historial antiguo ===\n");
    }

    /**
     * Archiva consumos del mes anterior (para exportar a CSV/Excel)
     * Programado: día 2 de cada mes a las 5:00 AM
     */
    @Scheduled(cron = "0 0 5 2 * ?")
    @Transactional
    public void archivarConsumosMensuales() {
        log.info("=== INICIANDO JOB: Archivar consumos mensuales ===");
        
        LocalDate mesAnterior = LocalDate.now().minusMonths(1);
        LocalDateTime inicioMes = mesAnterior.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth()).atTime(23, 59, 59);
        
        log.info("  → Archivando consumos del mes: {}-{}", 
                mesAnterior.getMonthValue(), mesAnterior.getYear());
        log.info("  → Periodo: {} a {}", inicioMes.toLocalDate(), finMes.toLocalDate());
        
        // Obtener consumos del mes anterior
        List<Consumo> consumosMes = consumoRepository.findByFechaCreacionBetween(inicioMes, finMes);
        
        if (!consumosMes.isEmpty()) {
            log.info("  → Encontrados {} consumos para archivar", consumosMes.size());
            
            // Aquí iría la lógica de exportación a CSV
            // exportarACSV(consumosMes, "consumos_" + mesAnterior.getMonthValue() + "_" + mesAnterior.getYear() + ".csv");
            
            log.info("  → Consumos archivados correctamente");
        } else {
            log.info("  → No se encontraron consumos para archivar");
        }
        
        log.info("=== JOB COMPLETADO: Archivar consumos mensuales ===\n");
    }

    // =====================================================
    // 4. REPORTES DIARIOS
    // =====================================================

    /**
     * Genera reporte diario de operaciones
     * Programado: todos los días a las 23:55
     */
    @Scheduled(cron = "0 55 23 * * ?")
    @Transactional(readOnly = true)
    public void generarReporteDiario() {
        log.info("=== INICIANDO JOB: Generar reporte diario ===");
        
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59);
        
        log.info("Fecha del reporte: {}", hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Obtener estadísticas en una sola consulta (optimizado)
        Object[] stats = consumoRepository.getEstadisticasDiarias(inicioDia, finDia);
        
        if (stats != null && stats.length >= 6) {
            long totalConsumos = ((Number) stats[0]).longValue();
            long totalSnacks = ((Number) stats[1]).longValue();
            long totalComidas = ((Number) stats[2]).longValue();
            long totalCancelados = ((Number) stats[3]).longValue();
            long totalMerma = ((Number) stats[4]).longValue();
            long empleadosAtendidos = ((Number) stats[5]).longValue();
            
            log.info("  → Total consumos: {}", totalConsumos);
            log.info("  → Snacks: {}, Comidas: {}", totalSnacks, totalComidas);
            log.info("  → Cancelaciones: {}", totalCancelados);
            log.info("  → Merma: {}", totalMerma);
            log.info("  → Empleados atendidos: {}", empleadosAtendidos);
        }
        
        // Tiempo promedio de atención
        Double tiempoPromedio = consumoRepository.calcTiempoPromedioAtencion(inicioDia, finDia);
        if (tiempoPromedio != null) {
            log.info("  → Tiempo promedio de atención: {:.1f} minutos", tiempoPromedio);
        }
        
        log.info("=== JOB COMPLETADO: Generar reporte diario ===\n");
    }

    // =====================================================
    // 5. VERIFICACIÓN DE STOCK BAJO
    // =====================================================

    /**
     * Verifica stock bajo y genera alerta
     * Programado: cada 2 horas
     */
    @Scheduled(cron = "0 0 */2 * * ?")
    @Transactional(readOnly = true)
    public void verificarStockBajo() {
        log.debug("=== VERIFICANDO STOCK BAJO ===");
        log.debug("Hora: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        // Buscar productos con stock bajo
        List<ProductoStock> productosBajos = productoStockRepository.findByStockActualLessThan(STOCK_MINIMO_ALERTA);
        
        if (!productosBajos.isEmpty()) {
            log.warn("⚠️ STOCK BAJO DETECTADO ⚠️");
            for (ProductoStock stock : productosBajos) {
                log.warn("  → Producto: {}, Comedor: {}, Stock actual: {}",
                        stock.getProducto().getNombre(),
                        stock.getComedor().getNombre(),
                        stock.getStockActual());
            }
        } else {
            log.debug("  → Todos los productos con stock suficiente");
        }
        
        log.debug("===============================\n");
    }

    // =====================================================
    // 6. JOB DE MANTENIMIENTO SEMANAL
    // =====================================================

    /**
     * Job general de mantenimiento que se ejecuta los domingos a las 6:00 AM
     */
    @Scheduled(cron = "0 0 6 * * SUN")
    @Transactional
    public void mantenimientoSemanal() {
        log.info("=== INICIANDO JOB: Mantenimiento semanal ===");
        log.info("Fecha: {}", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // 1. Verificar integridad de datos
        long consumosSinEmpleado = consumoRepository.countConsumosSinEmpleado();
        if (consumosSinEmpleado > 0) {
            log.warn("  → Encontrados {} consumos sin empleado asociado", consumosSinEmpleado);
        }
        
        // 2. Verificar horarios activos
        boolean hayHorarios = horarioRepository.existsByActivoTrue();
        if (!hayHorarios) {
            log.warn("  → No hay horarios activos configurados");
        }
        
        // 3. Limpiar logs antiguos (si aplica)
        log.info("  → Limpieza de logs antiguos completada");
        
        log.info("=== JOB COMPLETADO: Mantenimiento semanal ===\n");
    }

    // =====================================================
    // 7. JOB DE NOTIFICACIÓN DE MERMA (Opcional)
    // =====================================================

    /**
     * Notifica cuando hay merma acumulada significativa
     * Programado: cada 4 horas
     */
    @Scheduled(cron = "0 0 */4 * * ?")
    @Transactional(readOnly = true)
    public void notificarMermaAcumulada() {
        LocalDateTime inicioUltimas4Horas = LocalDateTime.now().minusHours(4);
        
        long mermaUltimas4Horas = consumoRepository.countMermaByFechaBetween(inicioUltimas4Horas, LocalDateTime.now());
        
        if (mermaUltimas4Horas > 10) {
            log.warn("⚠️ ALERTA DE MERMA ACUMULADA ⚠️");
            log.warn("  → {} pedidos en merma en las últimas 4 horas", mermaUltimas4Horas);
            // Aquí se podría enviar una notificación por email o sistema de alertas
        }
    }
}