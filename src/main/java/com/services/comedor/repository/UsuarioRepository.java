package com.services.comedor.repository;

import com.services.comedor.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 💻 APP USUARIO (POS/Kiosko)
    @Query("SELECT u FROM Usuario u " +
           "LEFT JOIN FETCH u.comedoresPermitidos " +
           "WHERE u.pin = :pin AND u.activo = true")
    Optional<Usuario> findByPinConComedores(@Param("pin") String pin);

    // 🔥 CONSULTA ESTRELLA PARA EL LOGIN STAFF (Corregida)
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.comedoresPermitidos WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    // 🔥 LA CONSULTA OPTIMIZADA (Corregida)
    @Query("SELECT COUNT(c) > 0 FROM Usuario u JOIN u.comedoresPermitidos c WHERE u.id = :usuarioId AND c.id = :comedorId")
    boolean tieneAccesoAComedor(@Param("usuarioId") Long usuarioId, @Param("comedorId") Long comedorId);

 }