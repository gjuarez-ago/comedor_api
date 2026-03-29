package com.services.comedor.repository;

import com.services.comedor.entity.ProductoDiaDisponible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoDiaDisponibleRepository extends JpaRepository<ProductoDiaDisponible, Long> {
    List<ProductoDiaDisponible> findByProductoIdAndComedorId(Long productoId, Long comedorId);
}

