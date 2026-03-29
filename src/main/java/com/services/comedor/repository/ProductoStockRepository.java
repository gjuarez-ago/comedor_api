package com.services.comedor.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.services.comedor.entity.Horario;
import com.services.comedor.entity.ProductoStock;

public interface ProductoStockRepository extends JpaRepository<ProductoStock, Long> {

    Optional<ProductoStock> findByComedorIdAndProductoId(Long comedorId, Long productoId);

    Optional<ProductoStock> findByProductoIdAndComedorId(Long productoId, Long comedorId);

    List<ProductoStock> findByProductoId(Long productoId);

    List<ProductoStock> findByComedorId(Long comedorId);


 /**
     * Busca horario activo según hora actual.
     * Índice recomendado: (comedor_id, activo, hora_inicio, hora_fin)
     */
    @Query("SELECT h FROM Horario h " +
           "WHERE h.comedor.id = :comedorId " +
           "AND h.activo = true " +
           "AND :horaActual BETWEEN h.horaInicio AND h.horaFin")
    Optional<Horario> findActiveByTime(
        @Param("comedorId") Long comedorId,
        @Param("horaActual") LocalTime horaActual
    );


    /**
 * Verifica si hay stock suficiente (para snacks o comidas con porciones)
 * 
 * @param productoId ID del producto
 * @param comedorId ID del comedor
 * @param cantidad Cantidad solicitada
 * @return true si hay stock suficiente
 */
@Query("SELECT COUNT(ps) > 0 FROM ProductoStock ps " +
       "WHERE ps.producto.id = :productoId " +
       "AND ps.comedor.id = :comedorId " +
       "AND ps.stockActual >= :cantidad")
boolean tieneStockSuficiente(
    @Param("productoId") Long productoId,
    @Param("comedorId") Long comedorId,
    @Param("cantidad") Integer cantidad
);

    /**
     * ✅ VALIDACIÓN: Verifica si existe configuración de stock.
     * Usa COUNT, no carga la entidad.
     */
    @Query("SELECT COUNT(ps) > 0 FROM ProductoStock ps " +
           "WHERE ps.producto.id = :productoId " +
           "AND ps.comedor.id = :comedorId")
    boolean existeConfiguracionStock(
        @Param("productoId") Long productoId,
        @Param("comedorId") Long comedorId
    );

    // =====================================================
    // OPERACIONES ATÓMICAS (UPDATE con validación incluida)
    // =====================================================

    /**
     * ✅ DESCUENTO ATÓMICO: Una sola consulta que valida y descuenta.
     * Retorna número de filas afectadas (1 = éxito, 0 = sin stock)
     */
    @Modifying
    @Query("UPDATE ProductoStock ps " +
           "SET ps.stockActual = ps.stockActual - :cantidad " +
           "WHERE ps.producto.id = :productoId " +
           "AND ps.comedor.id = :comedorId " +
           "AND ps.stockActual >= :cantidad")
    int descontarStockAtomico(
        @Param("productoId") Long productoId,
        @Param("comedorId") Long comedorId,
        @Param("cantidad") Integer cantidad
    );

    /**
     * ✅ DEVOLUCIÓN ATÓMICA: Una sola consulta.
     */
    @Modifying
    @Query("UPDATE ProductoStock ps " +
           "SET ps.stockActual = ps.stockActual + :cantidad " +
           "WHERE ps.producto.id = :productoId " +
           "AND ps.comedor.id = :comedorId")
    int devolverStockAtomico(
        @Param("productoId") Long productoId,
        @Param("comedorId") Long comedorId,
        @Param("cantidad") Integer cantidad
    );

       /**
     * ⚠️ Obtiene el stock actual de un producto en un comedor.
     * SOLO para reportes o administración.
     * NO usar en flujo de validación/descuento (usar tieneStockSuficiente en su lugar).
     * 
     * @return Optional con el stock actual, vacío si no existe configuración
     */
    @Query("SELECT ps.stockActual FROM ProductoStock ps " +
           "WHERE ps.producto.id = :productoId " +
           "AND ps.comedor.id = :comedorId")
    Optional<Integer> findStockActual(
        @Param("productoId") Long productoId,
        @Param("comedorId") Long comedorId
    );


     // =====================================================
    // ✅ VALIDACIÓN MULTIPLE (OPTIMIZADA)
    // =====================================================

    /**
     * ✅ VALIDACIÓN DE STOCK: Una consulta para todos los productos.
     * 
     * Retorna lista con:
     *   [0] producto_id
     *   [1] nombre
     *   [2] stock_actual
     *   [3] cantidad_solicitada
     *   [4] hay_stock (boolean)
     * 
     * @param productosIds Lista de IDs de productos a validar
     * @param comedorId ID del comedor
     * @param cantidad Cantidad solicitada (se aplica a todos)
     * @return Lista de arreglos con información de stock
     */
    @Query(value = """
        SELECT 
            p.id,
            p.nombre,
            COALESCE(ps.stock_actual, 0) AS stock,
            :cantidad AS solicitado,
            CASE WHEN COALESCE(ps.stock_actual, 0) >= :cantidad THEN true ELSE false END AS hay_stock
        FROM productos p
        INNER JOIN comedor_productos cp ON cp.producto_id = p.id AND cp.comedor_id = :comedorId
        LEFT JOIN producto_stock ps ON ps.producto_id = p.id AND ps.comedor_id = :comedorId
        WHERE p.id IN (:productosIds)
          AND p.controla_inventario = true
        """, nativeQuery = true)
    List<Object[]> validarStockMultiple(
        @Param("productosIds") List<Long> productosIds,
        @Param("comedorId") Long comedorId,
        @Param("cantidad") Integer cantidad
    );

    /**
 * Busca productos con stock por debajo del umbral
 * Usado en verificarStockBajo()
 */
List<ProductoStock> findByStockActualLessThan(int stockMinimo);
}
