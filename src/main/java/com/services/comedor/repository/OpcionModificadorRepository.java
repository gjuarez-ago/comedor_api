package com.services.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.comedor.entity.OpcionModificador;

public interface OpcionModificadorRepository extends  JpaRepository<OpcionModificador, Long>{
    
}
