package com.services.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.comedor.entity.ConsumoEstadoHistorial;

public interface ConsumoEstadoHistorialRepository  extends  JpaRepository<ConsumoEstadoHistorial, Long>{
    
}
