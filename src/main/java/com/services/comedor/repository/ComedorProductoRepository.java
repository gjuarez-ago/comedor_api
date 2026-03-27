package com.services.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.services.comedor.entity.ComedorProducto;

public interface ComedorProductoRepository extends JpaRepository<ComedorProducto, Long> {

    // 🍽️ CATÁLOGO FILTRADO POR TURNO
    @Query("SELECT cp FROM ComedorProducto cp " +
           "JOIN FETCH cp.producto p " +
           "JOIN cp.turnosDisponibles t " + 
           "WHERE cp.comedor.id = :comedorId " +
           "AND t.id = :tipoConsumoId " +   
           "AND cp.disponible = true " +
           "AND p.activo = true")
    List<ComedorProducto> findMenuPorComedorYTurno(
        @Param("comedorId") Long comedorId,
        @Param("tipoConsumoId") Long tipoConsumoId
    );
    
}