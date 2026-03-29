package com.services.comedor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.cache.annotation.Cacheable;

import com.services.comedor.entity.ComedorProducto;

public interface ComedorProductoRepository extends JpaRepository<ComedorProducto, Long> {

     /**
     * 🔥 CONSULTA NIVEL ENTERPRISE (100,000 usuarios) 
     * Trae la configuración local del comedor, el producto maestro y todos sus modificadores
     * en UNA SOLA ida a la base de datos.
     * 
     * 🔥 INCLUYE FILTRO POR DÍA DE LA SEMANA
     * 
     * @param comedorId ID del comedor
     * @param diaSemana Día de la semana (1=Lunes, 2=Martes, ..., 7=Domingo)
     * @return Lista de productos disponibles para ese comedor y día
     */
    @Cacheable(value = "menu", key = "#comedorId")  // 🔥 CACHE
    @Query(value = """
        SELECT DISTINCT 
            cp.id,
            p.id AS producto_id,
            p.nombre,
            p.descripcion,
            p.imagen_url,
            cp.precio_empleado,
            p.controla_inventario,
            COALESCE(ps.stock_actual, 0) AS stock_actual,
            tc.id AS tipo_consumo_id
        FROM comedor_productos cp
        INNER JOIN productos p ON p.id = cp.producto_id
        INNER JOIN comedor_producto_turnos cpt ON cpt.comedor_producto_id = cp.id
        INNER JOIN tipos_consumo tc ON tc.id = cpt.tipo_consumo_id
        LEFT JOIN producto_stock ps ON ps.producto_id = p.id AND ps.comedor_id = cp.comedor_id
        LEFT JOIN producto_dias_disponibles pdd ON pdd.producto_id = p.id 
            AND pdd.comedor_id = cp.comedor_id 
            AND pdd.dia_semana = :diaSemana
        WHERE cp.comedor_id = :comedorId
          AND cp.disponible = true
          AND p.activo = true
          AND (p.controla_inventario = false OR (ps.stock_actual IS NOT NULL AND ps.stock_actual > 0))
          AND (pdd.id IS NULL OR pdd.disponible = true)
        ORDER BY tc.id, p.id
        """, nativeQuery = true)
    List<Object[]> findMenuRawByDay(
            @Param("comedorId") Long comedorId,
            @Param("diaSemana") Integer diaSemana);

     /**
     * ✅ Busca la configuración de un producto en un comedor específico.
     * Útil para obtener precios y disponibilidad.
     * 
     * @param comedorId ID del comedor
     * @param productoId ID del producto
     * @return Optional con la configuración del producto en ese comedor
     */
    @Query("SELECT cp FROM ComedorProducto cp " +
           "WHERE cp.comedor.id = :comedorId " +
           "AND cp.producto.id = :productoId " +
           "AND cp.disponible = true")
    Optional<ComedorProducto> findByComedorIdAndProductoId(
        @Param("comedorId") Long comedorId,
        @Param("productoId") Long productoId
    );

    List<ComedorProducto> findByProductoId(Long productoId);

    Optional<ComedorProducto> findByProductoIdAndComedorId(Long productoId, Long comedorId);

}
