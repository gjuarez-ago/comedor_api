package com.services.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.comedor.entity.ConsumoDetalle;

public interface  ConsumoDetalleRepository  extends  JpaRepository<ConsumoDetalle, Long>{
    
}
