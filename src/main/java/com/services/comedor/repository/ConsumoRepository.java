package com.services.comedor.repository;

import com.services.comedor.entity.Consumo;
import com.services.comedor.enums.EstadoConsumo;
import com.services.comedor.models.ConsumoResumenDTO;
import com.services.comedor.models.MermaDTO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    // =====================================================
    // 1. VALIDACIONES RÁPIDAS
    // =====================================================
    /**
     * Verifica si un empleado ya consumió un tipo específico hoy.
     */
    @Query("SELECT COUNT(c) > 0 FROM Consumo c "
            + "WHERE c.empleado.id = :empleadoId "
            + "AND c.tipoConsumo.id = :tipoConsumoId "
            + "AND c.fecha = :fecha "
            + "AND c.estado NOT IN ('CANCELADO', 'ENTREGADO')")
    boolean yaConsumioHoy(
            @Param("empleadoId") Long empleadoId,
            @Param("tipoConsumoId") Long tipoConsumoId,
            @Param("fecha") LocalDate fecha
    );

    /**
     * ✅ Cuenta cuántos tipos diferentes ha consumido el empleado hoy.
     */
    @Query(value = """
        SELECT COUNT(DISTINCT c.tipo_consumo_id) 
        FROM consumos c
        WHERE c.empleado_id = :empleadoId
          AND DATE(c.fecha_creacion) = CURDATE()
          AND c.tipo_consumo_id NOT IN (99)
          AND c.estado NOT IN ('CANCELADO', 'ENTREGADO')
        """, nativeQuery = true)
    int countTiposConsumidosHoy(@Param("empleadoId") Long empleadoId);

    /**
     * Verifica si un QR es válido para escanear.
     */
    @Query("SELECT COUNT(c) > 0 FROM Consumo c "
            + "WHERE c.tokenQr = :tokenQr "
            + "AND c.comedor.id = :comedorId "
            + "AND c.fecha = :fechaActual "
            + "AND c.estado = 'CREADO'")
    boolean esQrValidoParaEscanear(
            @Param("tokenQr") String tokenQr,
            @Param("comedorId") Long comedorId,
            @Param("fechaActual") LocalDate fechaActual
    );

    // =====================================================
    // 2. OPERACIONES CRÍTICAS CON BLOQUEO
    // =====================================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Consumo c "
            + "JOIN FETCH c.empleado "
            + "JOIN FETCH c.tipoConsumo "
            + "WHERE c.tokenQr = :tokenQr")
    Optional<Consumo> findByTokenQrForUpdate(@Param("tokenQr") String tokenQr);

    // =====================================================
    // 3. PANTALLAS OPERATIVAS
    // =====================================================
    @Query("SELECT DISTINCT c FROM Consumo c "
            + "JOIN FETCH c.detalles d "
            + "JOIN FETCH d.producto p "
            + "LEFT JOIN FETCH d.modificadores m "
            + "WHERE c.comedor.id = :comedorId "
            + "AND c.estado IN (:estados) "
            + "ORDER BY c.fechaCreacion ASC")
    List<Consumo> findConsumosParaCocina(
            @Param("comedorId") Long comedorId,
            @Param("estados") List<EstadoConsumo> estados
    );

    // =====================================================
    // 4. APP DEL EMPLEADO
    // =====================================================
    @Query("SELECT c FROM Consumo c "
            + "JOIN FETCH c.tipoConsumo "
            + "WHERE c.empleado.id = :empleadoId "
            + "AND c.fecha = :fecha "
            + "AND c.estado NOT IN ('ENTREGADO', 'CANCELADO')")
    Optional<Consumo> findMiTicketActivo(
            @Param("empleadoId") Long empleadoId,
            @Param("fecha") LocalDate fecha
    );

    @Query("SELECT COUNT(c) FROM Consumo c "
            + "WHERE c.comedor.id = :comedorId "
            + "AND c.estado = 'PAGADO' "
            + "AND c.fechaCreacion < :miFechaCreacion")
    Integer countPedidosAdelante(
            @Param("comedorId") Long comedorId,
            @Param("miFechaCreacion") LocalDateTime miFechaCreacion
    );

    /**
     * ✅ CONSULTA ULTRALIGERA: Verifica si el empleado tiene un pedido activo.
     */
    @Query("SELECT c.estado FROM Consumo c "
            + "WHERE c.empleado.id = :empleadoId "
            + "AND c.fecha = :fecha "
            + "AND c.estado NOT IN ('ENTREGADO', 'CANCELADO')")
    Optional<EstadoConsumo> findEstadoPedidoActivo(
            @Param("empleadoId") Long empleadoId,
            @Param("fecha") LocalDate fecha
    );

    @Query("SELECT DISTINCT c FROM Consumo c "
            + "JOIN FETCH c.empleado e "
            + "JOIN FETCH c.detalles d "
            + "JOIN FETCH d.producto p "
            + "LEFT JOIN FETCH d.modificadores m "
            + "WHERE c.comedor.id = :comedorId "
            + "AND c.fecha = :hoy "
            + "AND c.estado IN ('CREADO', 'PAGADO', 'PREPARANDO', 'LISTO') "
            + "ORDER BY CASE c.estado "
            + "  WHEN 'LISTO' THEN 1 "
            + "  WHEN 'PREPARANDO' THEN 2 "
            + "  WHEN 'PAGADO' THEN 3 "
            + "  WHEN 'CREADO' THEN 4 "
            + "END, c.fechaCreacion ASC")
    List<Consumo> findPanelDespachoOptimizado(
            @Param("comedorId") Long comedorId,
            @Param("hoy") LocalDate hoy
    );

    // =====================================================
    // 5. BÚSQUEDAS POR ESTADO Y COMEDOR
    // =====================================================
    /**
     * Busca consumos por comedor y estado
     */
    List<Consumo> findByComedorIdAndEstado(Long comedorId, EstadoConsumo estado);

    // =====================================================
    // 6. BÚSQUEDAS POR FECHA (NUEVO)
    // =====================================================
    /**
     * Busca consumos por rango de fechas
     */
    List<Consumo> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca consumos anteriores a una fecha
     */
    List<Consumo> findByFechaCreacionBefore(LocalDateTime fechaLimite);

    // =====================================================
    // 7. MANTENIMIENTO Y JOBS
    // =====================================================
    /**
     * Cancela QRs no escaneados para un tipo de consumo y fecha específicos
     */
    @Modifying
    @Query("UPDATE Consumo c SET c.estado = 'CANCELADO', "
            + "c.motivoCancelacion = 'Expirado por falta de escaneo', "
            + "c.fechaCancelacion = CURRENT_TIMESTAMP "
            + "WHERE c.estado = 'CREADO' "
            + "AND c.tipoConsumo.id = :tipoConsumoId "
            + "AND c.fecha = :fecha")
    int cancelarQrsNoEscaneados(
            @Param("tipoConsumoId") Long tipoConsumoId,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Marca como merma pedidos en estado LISTO o PREPARANDO que llevan más de X
     * tiempo
     */
    @Modifying
    @Query("UPDATE Consumo c SET c.esMerma = true "
            + "WHERE c.estado IN ('PREPARANDO', 'LISTO') "
            + "AND c.fechaCreacion < :fechaLimite")
    int marcarMermaAutomatica(@Param("fechaLimite") LocalDateTime fechaLimite);

    // =====================================================
    // 8. PROYECCIONES PARA JOBS Y REPORTES
    // =====================================================
    /**
     * Busca IDs de consumos antiguos (solo IDs, no entidades completas)
     */
    @Query("SELECT c.id FROM Consumo c "
            + "WHERE c.fechaCreacion < :fechaLimite")
    List<Long> findIdsByFechaCreacionBefore(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT new com.services.comedor.models.ConsumoResumenDTO("
            + "c.id, c.tokenQr, c.estado, c.fechaCreacion, "
            + "e.id, e.nombre, e.numeroEmpleado, "
            + "tc.id, tc.nombre) "
            + "FROM Consumo c "
            + "JOIN c.empleado e "
            + "JOIN c.tipoConsumo tc "
            + "WHERE c.fechaCreacion BETWEEN :inicio AND :fin")
    List<ConsumoResumenDTO> findResumenByFechaCreacionBetween(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Busca mermas con datos esenciales
     */
    @Query("SELECT new com.services.comedor.models.MermaDTO("
            + "c.id, c.tokenQr, c.fechaCreacion, "
            + "e.id, e.nombre, e.numeroEmpleado, "
            + "tc.id, tc.nombre, "
            + "COUNT(d.id)) "
            + "FROM Consumo c "
            + "JOIN c.empleado e "
            + "JOIN c.tipoConsumo tc "
            + "LEFT JOIN c.detalles d "
            + "WHERE c.estado IN ('PREPARANDO', 'LISTO') "
            + "AND c.fechaCreacion < :fechaLimite "
            + "GROUP BY c.id, e.id, e.nombre, e.numeroEmpleado, tc.id, tc.nombre")
    List<MermaDTO> findMermasOptimizadas(@Param("fechaLimite") LocalDateTime fechaLimite);

    // =====================================================
    // 9. CONTADORES OPTIMIZADOS
    // =====================================================
    @Query("SELECT COUNT(c) FROM Consumo c "
            + "WHERE c.fechaCreacion BETWEEN :inicio AND :fin")
    long countByFechaCreacionBetween(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Consumo c "
            + "WHERE c.fechaCreacion BETWEEN :inicio AND :fin "
            + "AND c.tipoConsumo.id = :tipoTiendaId")
    long countSnacksByFecha(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("tipoTiendaId") Long tipoTiendaId);

    @Query("SELECT COUNT(c) FROM Consumo c "
            + "WHERE c.estado = :estado "
            + "AND c.fechaCreacion BETWEEN :inicio AND :fin")
    long countByEstadoAndFechaBetween(@Param("estado") EstadoConsumo estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Consumo c "
            + "WHERE c.esMerma = true "
            + "AND c.fechaCreacion BETWEEN :inicio AND :fin")
    long countByEsMermaTrueAndFechaBetween(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(DISTINCT c.empleado.id) FROM Consumo c "
            + "WHERE c.fechaCreacion BETWEEN :inicio AND :fin")
    long countDistinctEmpleadosByFecha(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Consumo c "
            + "WHERE c.esMerma = true "
            + "AND c.fechaCreacion BETWEEN :inicio AND :fin")
    long countMermaByFechaBetween(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Consumo c WHERE c.empleado IS NULL")
    long countConsumosSinEmpleado();

    // =====================================================
    // 10. CÁLCULOS AGREGADOS
    // =====================================================
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, c.fechaCreacion, c.fechaCancelacion)) "
            + "FROM Consumo c "
            + "WHERE c.estado = 'ENTREGADO' "
            + "AND c.fechaCreacion BETWEEN :inicio AND :fin")
    Double calcTiempoPromedioAtencion(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query(value = """
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN tc.id = 99 THEN 1 ELSE 0 END) as snacks,
            SUM(CASE WHEN tc.id != 99 THEN 1 ELSE 0 END) as comidas,
            SUM(CASE WHEN c.estado = 'CANCELADO' THEN 1 ELSE 0 END) as cancelados,
            SUM(CASE WHEN c.es_merma = true THEN 1 ELSE 0 END) as merma,
            COUNT(DISTINCT c.empleado_id) as empleados
        FROM consumos c
        INNER JOIN tipos_consumo tc ON tc.id = c.tipo_consumo_id
        WHERE c.fecha_creacion BETWEEN :inicio AND :fin
        """, nativeQuery = true)
    Object[] getEstadisticasDiarias(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query(value = """
        SELECT 
            HOUR(c.fecha_creacion) as hora,
            COUNT(*) as total,
            SUM(CASE WHEN tc.id = 99 THEN 1 ELSE 0 END) as snacks,
            SUM(CASE WHEN tc.id != 99 THEN 1 ELSE 0 END) as comidas
        FROM consumos c
        INNER JOIN tipos_consumo tc ON tc.id = c.tipo_consumo_id
        WHERE c.fecha_creacion BETWEEN :inicio AND :fin
        GROUP BY HOUR(c.fecha_creacion)
        ORDER BY hora ASC
        """, nativeQuery = true)
    List<Object[]> getEstadisticasPorHora(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // =====================================================
    // 11. ELIMINACIÓN MASIVA
    // =====================================================
    @Modifying
    @Query("DELETE FROM Consumo c WHERE c.id IN :ids")
    int deleteAllByIdIn(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM Consumo c WHERE c.fechaCreacion < :fechaLimite")
    int deleteByFechaCreacionBefore(@Param("fechaLimite") LocalDateTime fechaLimite);

    // =====================================================
// MÉTODOS FALTANTES PARA SistemaJobs
// =====================================================
    /**
     * Busca mermas por fecha límite (retorna entidades completas) Usado en
     * marcarMermaAutomatica() para log detallado
     */
    @Query("SELECT c FROM Consumo c "
            + "JOIN FETCH c.empleado "
            + "JOIN FETCH c.detalles d "
            + "WHERE c.estado IN ('PREPARANDO', 'LISTO') "
            + "AND c.fechaCreacion < :fechaLimite")
    List<Consumo> findMermasByFechaLimite(@Param("fechaLimite") LocalDateTime fechaLimite);
}
