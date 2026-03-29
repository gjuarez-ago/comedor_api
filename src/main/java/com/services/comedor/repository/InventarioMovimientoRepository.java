package com.services.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.comedor.entity.InventarioMovimiento;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {
    
}
