package com.services.comedor.repository;

import com.services.comedor.entity.Usuario;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 💻 APP USUARIO (POS/Kiosko)
    @Query("SELECT u FROM Usuario u "
            + "LEFT JOIN FETCH u.comedoresPermitidos "
            + "WHERE u.pin = :pin AND u.activo = true")
    Optional<Usuario> findByPinConComedores(@Param("pin") String pin);

    // MÉTODOS DE EXISTENCIA
    boolean existsByPin(String pin);

    /**
     * Busca usuario por username con sus comedores permitidos.
     */
    @EntityGraph(attributePaths = {"comedorBase", "comedoresPermitidos"})
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si un usuario tiene acceso a un comedor específico.
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u "
            + "JOIN u.comedoresPermitidos c "
            + "WHERE u.id = :usuarioId AND c.id = :comedorId")
    boolean tieneAccesoAComedor(
            @Param("usuarioId") Long usuarioId,
            @Param("comedorId") Long comedorId
    );

    /**
     * Verifica si username ya existe.
     */
    boolean existsByUsername(String username);

    List<Usuario> findByActivoTrue();

}
