package com.services.comedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.services.comedor.entity.Comedor;

public interface  ComedorRepository extends JpaRepository<Comedor, Long> {
    boolean existsByNombre(String nombre);
    
     /**
     * Obtiene menú completo para un comedor.
     * Una sola consulta que trae productos, precios, tipos y stock.
     * Es la consulta más crítica del sistema (cacheada).
     */
    @Query(value = """
        SELECT 
            p.id AS producto_id,
            p.nombre AS producto_nombre,
            p.descripcion,
            p.imagen_url,
            p.requiere_preparacion,
            p.controla_inventario,
            cp.precio_empleado,
            cp.precio_empresa,
            COALESCE(ps.stock_actual, 0) AS stock_actual,
            tc.id AS tipo_consumo_id,
            tc.nombre AS tipo_consumo_nombre
        FROM comedor_productos cp
        INNER JOIN productos p ON p.id = cp.producto_id
        INNER JOIN comedor_producto_turnos cpt ON cpt.comedor_producto_id = cp.id
        INNER JOIN tipos_consumo tc ON tc.id = cpt.tipo_consumo_id
        LEFT JOIN producto_stock ps ON ps.producto_id = p.id AND ps.comedor_id = cp.comedor_id
        WHERE cp.comedor_id = :comedorId
          AND cp.disponible = true
          AND p.activo = true
          AND (p.controla_inventario = false OR ps.stock_actual > 0)
        ORDER BY tc.id, p.id
        """, nativeQuery = true)
    List<Object[]> findMenuCompleto(@Param("comedorId") Long comedorId);

    /**
     * Obtiene el tipo de consumo de un producto en un comedor específico.
     */
    /**
     * Obtiene los tipos de consumo disponibles para un producto en un comedor.
     * ✅ CORREGIDO: cpt ya es TipoConsumo, accedes directamente a su id.
     */
    @Query("SELECT cpt.id FROM ComedorProducto cp " +
           "JOIN cp.turnosDisponibles cpt " +
           "WHERE cp.comedor.id = :comedorId " +
           "AND cp.producto.id = :productoId " +
           "AND cp.disponible = true")
    List<Long> findTiposByProductoAndComedor(
        @Param("productoId") Long productoId,
        @Param("comedorId") Long comedorId
    );

        List<Comedor> findByActivoTrue();

    
}
