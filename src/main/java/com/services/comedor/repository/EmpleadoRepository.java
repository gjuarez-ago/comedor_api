package com.services.comedor.repository;

import java.time.LocalTime;
import java.util.List;

import com.services.comedor.entity.Empleado;
import com.services.comedor.models.EmpleadoDTO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // 📱 APP EMPLEADO (Comensal)
    // Para iniciar sesión en la app móvil.
    Optional<Empleado> findByTelefonoAndPinAndActivoTrue(String telefono, String pin);

    // 💻 APP USUARIO (POS/Caja)
    // Por si el empleado olvida el teléfono y el cajero digita su número de nómina.
    Optional<Empleado> findByNumeroEmpleadoAndActivoTrue(String numeroEmpleado);

    /**
     * Busca empleado por número con sus permisos cargados. Usa EntityGraph para
     * evitar N+1.
     */
    @EntityGraph(attributePaths = {"consumosPermitidos", "comedor"})
    Optional<Empleado> findByNumeroEmpleado(String numeroEmpleado);

    /**
     * // 📱 APP EMPLEADO (Comensal) // Para iniciar sesión en la app móvil.
     * Busca empleado por teléfono con sus permisos cargados.
     */
    @EntityGraph(attributePaths = {"consumosPermitidos", "comedor"})
    Optional<Empleado> findByTelefono(String telefono);

    /**
     * Verifica si existe empleado por número.
     */
    boolean existsByNumeroEmpleado(String numeroEmpleado);

    /**
     * Verifica si existe empleado por teléfono.
     */
    boolean existsByTelefono(String telefono);

    List<Empleado> findByActivoTrue();

    /**
     * Verifica si un empleado tiene permiso para un tipo de consumo. Consulta
     * rápida sin cargar toda la entidad.
     */
    @Query("SELECT COUNT(e) > 0 FROM Empleado e "
            + "JOIN e.consumosPermitidos cp "
            + "WHERE e.id = :empleadoId "
            + "AND cp.id = :tipoConsumoId")
    boolean tienePermisoConsumo(
            @Param("empleadoId") Long empleadoId,
            @Param("tipoConsumoId") Long tipoConsumoId
    );

    // En EmpleadoRepository.java
    /**
     * ✅ VALIDACIÓN UNIFICADA: Una sola consulta que valida TODAS las
     * precondiciones para generar un pedido. Retorna un arreglo con todos los
     * resultados.
     *
     * @return Object[] con: [0] empleado_activo (boolean) [1] horario_flexible
     * (boolean) [2] tiene_permiso (boolean) [3] en_horario (boolean) [4]
     * ya_consumio (boolean) [5] consumidos_hoy (int) [6] hora_inicio (Time) -
     * solo si en_horario es false [7] hora_fin (Time) - solo si en_horario es
     * false
     */
    @Query(value = """
    SELECT 
        e.activo,
        COALESCE(e.horario_flexible, false),
        EXISTS(SELECT 1 FROM empleado_permisos_consumo ep 
               WHERE ep.empleado_id = e.id AND ep.tipo_consumo_id = :tipoConsumoId),
        EXISTS(SELECT 1 FROM horarios h 
               WHERE h.comedor_id = e.comedor_id 
                 AND h.tipo_consumo_id = :tipoConsumoId 
                 AND h.activo = true 
                 AND :horaActual BETWEEN h.hora_inicio AND h.hora_fin),
        EXISTS(SELECT 1 FROM consumos c 
               WHERE c.empleado_id = e.id 
                 AND c.tipo_consumo_id = :tipoConsumoId 
                 AND c.fecha = CURDATE() 
                 AND c.estado NOT IN ('CANCELADO', 'ENTREGADO')),
        (SELECT COUNT(DISTINCT c.tipo_consumo_id) FROM consumos c 
         WHERE c.empleado_id = e.id 
           AND DATE(c.fecha_creacion) = CURDATE() 
           AND c.tipo_consumo_id NOT IN (99) 
           AND c.estado NOT IN ('CANCELADO', 'ENTREGADO')),
        h.hora_inicio,
        h.hora_fin
    FROM empleados e
    LEFT JOIN horarios h ON h.comedor_id = e.comedor_id 
                        AND h.tipo_consumo_id = :tipoConsumoId 
                        AND h.activo = true
    WHERE e.id = :empleadoId
    """, nativeQuery = true)
    Object[] validarPreCondicionesPedido(
            @Param("empleadoId") Long empleadoId,
            @Param("tipoConsumoId") Long tipoConsumoId,
            @Param("horaActual") LocalTime horaActual
    );

      // =====================================================
    // BÚSQUEDA UNIFICADA (número, nombre o teléfono)
    // =====================================================

    /**
     * Búsqueda unificada: busca por número de empleado, nombre o teléfono
     * 
     * @param termino Texto a buscar (puede ser número de empleado, nombre o teléfono)
     * @param pageable Paginación (máx 10 resultados)
     * @return Lista de empleados que coinciden
     */
    @Query("SELECT new com.services.comedor.models.EmpleadoDTO(" +
           "e.id, e.numeroEmpleado, e.nombre, e.telefono, c.nombre, e.horarioFlexible) " +
           "FROM Empleado e " +
           "JOIN e.comedor c " +
           "WHERE e.activo = true " +
           "AND (e.numeroEmpleado LIKE CONCAT(:termino, '%') " +
           "     OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "     OR e.telefono LIKE CONCAT(:termino, '%')) " +
           "ORDER BY e.numeroEmpleado ASC")
    List<EmpleadoDTO> searchEmpleados(@Param("termino") String termino,
                                       Pageable pageable);


}
