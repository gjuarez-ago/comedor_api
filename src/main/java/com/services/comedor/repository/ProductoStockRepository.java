package com.services.comedor.repository;

import com.services.comedor.entity.ProductoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductoStockRepository extends JpaRepository<ProductoStock, Long> {

    Optional<ProductoStock> findByComedorIdAndProductoId(Long comedorId, Long productoId);

    // 🔥 UPDATE ATÓMICO: Descuenta stock directo en la base de datos sin cargar la entidad.
    // Evita errores de concurrencia si dos personas compran la última Coca-Cola al mismo tiempo.
    @Modifying
    @Query("UPDATE ProductoStock ps SET ps.stockActual = ps.stockActual - :cantidad " +
           "WHERE ps.comedor.id = :comedorId AND ps.producto.id = :productoId " +
           "AND ps.stockActual >= :cantidad")
    int descontarStockFisico(
        @Param("comedorId") Long comedorId, 
        @Param("productoId") Long productoId, 
        @Param("cantidad") Integer cantidad
    );
}