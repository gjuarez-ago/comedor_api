package com.services.comedor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.services.comedor.entity.TipoConsumo;

@Repository
public interface TipoConsumoRepository extends JpaRepository<TipoConsumo, Long> {

    Optional<TipoConsumo> findByNombre(String nombre);


    boolean existsByNombre(String nombre);


}