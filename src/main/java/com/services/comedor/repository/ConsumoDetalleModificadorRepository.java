package com.services.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.comedor.entity.ConsumoDetalleModificador;

public interface ConsumoDetalleModificadorRepository  extends  JpaRepository<ConsumoDetalleModificador, Long>{
    
}
