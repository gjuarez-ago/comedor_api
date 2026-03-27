package com.services.comedor.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.services.comedor.entity.Consumo;
import com.services.comedor.enums.EstadoConsumo;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    // 🔥 1. ANTI-FRAUDE (Validación rápida)
    // No cargamos la entidad, solo le preguntamos a la BD si "existe" el registro.
    // Esto toma milisegundos y no consume RAM.
    @Query("SELECT COUNT(c) > 0 FROM Consumo c " +
           "WHERE c.empleado.id = :empleadoId " +
           "AND c.tipoConsumo.id = :tipoConsumoId " +
           "AND c.fecha = :fecha " +
           "AND c.estado <> 'CANCELADO'")
    boolean yaConsumioEnEsteTurno(
        @Param("empleadoId") Long empleadoId, 
        @Param("tipoConsumoId") Long tipoConsumoId, 
        @Param("fecha") LocalDate fecha
    );

    // 📷 2. ESCANEO DE QR (Bloqueo Transaccional Crítico)
    // @Lock hace un "SELECT ... FOR UPDATE" en PostgreSQL/MySQL.
    // Si 2 cajeros escanean el QR al mismo milisegundo, la BD bloquea la segunda petición.
    // Usamos JOIN FETCH para traer al empleado y tipoConsumo en 1 sola query (Adiós N+1).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Consumo c " +
           "JOIN FETCH c.empleado " +
           "JOIN FETCH c.tipoConsumo " +
           "WHERE c.tokenQr = :tokenQr")
    Optional<Consumo> findByTokenQrForUpdate(@Param("tokenQr") String tokenQr);

    // 🍳 3. KDS (PANTALLA DE COCINA) - Súper Optimizado
    // Usamos DISTINCT y JOIN FETCH para traer el ticket, sus detalles y el producto
    // en una sola vuelta a la base de datos.
    @Query("SELECT DISTINCT c FROM Consumo c " +
           "JOIN FETCH c.detalles d " +
           "JOIN FETCH d.producto p " +
           "WHERE c.comedor.id = :comedorId " +
           "AND c.estado IN (:estados) " +
           "ORDER BY c.fechaCreacion ASC")
    List<Consumo> findConsumosParaCocina(
        @Param("comedorId") Long comedorId, 
        @Param("estados") List<EstadoConsumo> estados
    );

    // 📱 APP EMPLEADO: Trae el ticket actual para mostrar el QR en la pantalla principal
    @Query("SELECT c FROM Consumo c " +
           "JOIN FETCH c.tipoConsumo " +
           "WHERE c.empleado.id = :empleadoId " +
           "AND c.fecha = :fecha " +
           "AND c.estado NOT IN ('ENTREGADO', 'CANCELADO')")
    Optional<Consumo> findMiTicketActivo(
        @Param("empleadoId") Long empleadoId, 
        @Param("fecha") LocalDate fecha
    );

  

    // 💻 APP USUARIO (Caja)
    // Antes de procesar, validamos que el QR sea "fresco". 
    // No queremos que alguien use un QR que generó ayer y no usó.
    @Query("SELECT COUNT(c) > 0 FROM Consumo c " +
           "WHERE c.tokenQr = :tokenQr " +
           "AND c.comedor.id = :comedorId " +
           "AND c.fecha = :fechaActual " +
           "AND c.estado = 'CREADO'")
    boolean esQrValidoParaEscanear(
        @Param("tokenQr") String tokenQr, 
        @Param("comedorId") Long comedorId, 
        @Param("fechaActual") LocalDate fechaActual
    );

    // ⚙️ SISTEMA (Tarea Automática / Admin)
    // Cancela todos los QRs que se generaron pero nunca se escanearon al terminar el horario.
    @Modifying
    @Query("UPDATE Consumo c SET c.estado = 'CANCELADO', c.motivoCancelacion = 'Expirado por falta de escaneo' " +
           "WHERE c.estado = 'CREADO' " +
           "AND c.tipoConsumo.id = :tipoConsumoId " +
           "AND c.fecha = :fecha")
    int cancelarQrsNoEscaneados(
        @Param("tipoConsumoId") Long tipoConsumoId, 
        @Param("fecha") LocalDate fecha
    );

    // 📱 APP EMPLEADO
    // Busca el ticket del empleado para el día de hoy que aún no se haya entregado.
    @Query("SELECT c FROM Consumo c " +
           "WHERE c.empleado.id = :empleadoId " +
           "AND c.fecha = :hoy " +
           "AND c.estado NOT IN ('ENTREGADO', 'CANCELADO')")
    Optional<Consumo> findTicketActivo(
        @Param("empleadoId") Long empleadoId, 
        @Param("hoy") java.time.LocalDate hoy
    );

    // 📊 EXTRA: Saber cuántos pedidos hay antes que el suyo (PAGADOS)
    @Query("SELECT COUNT(c) FROM Consumo c " +
           "WHERE c.comedor.id = :comedorId " +
           "AND c.estado = 'PAGADO' " +
           "AND c.fechaCreacion < :miFechaCreacion")
    Integer countPedidosAdelante(
        @Param("comedorId") Long comedorId, 
        @Param("miFechaCreacion") java.time.LocalDateTime miFechaCreacion
    );

    /**
     * 🖥️ VISTA DEL DESPACHADOR
     * Trae pedidos en estados de gestión activa.
     * JOIN FETCH encadenado para eliminar el N+1 de detalles y modificadores.
     */
   @Query("SELECT DISTINCT c FROM Consumo c " +
           "JOIN FETCH c.empleado e " +
           "JOIN FETCH c.detalles d " +
           "JOIN FETCH d.producto p " +
           "LEFT JOIN FETCH d.modificadores m " +
           "WHERE c.comedor.id = :comedorId " +
           "AND c.fecha = :hoy " +
           "AND c.estado IN ('CREADO', 'PAGADO', 'PREPARANDO', 'LISTO') " + // 🔥 Incluimos PAGADO
           "ORDER BY CASE c.estado " +
           "  WHEN 'LISTO' THEN 1 " +      // 1. ¡Entregar ya!
           "  WHEN 'PREPARANDO' THEN 2 " + // 2. En el fuego
           "  WHEN 'PAGADO' THEN 3 " +    // 3. 🆕 En fila de espera
           "  WHEN 'CREADO' THEN 4 " +      // 4. Solo referencia (vienen en camino)
           "END, c.fechaCreacion ASC")
    List<Consumo> findPanelDespachoOptimizado(
        @Param("comedorId") Long comedorId, 
        @Param("hoy") java.time.LocalDate hoy
    );
}