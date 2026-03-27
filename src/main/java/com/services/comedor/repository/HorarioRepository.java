package com.services.comedor.repository;

import com.services.comedor.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;
import java.util.Optional;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    // 📱 APP y 💻 POS
    // Recibe la hora actual del servidor y te dice si es Desayuno, Comida o Cena.
    @Query("SELECT h FROM Horario h " +
           "JOIN FETCH h.tipoConsumo " +
           "WHERE h.comedor.id = :comedorId " +
           "AND :horaActual BETWEEN h.horaInicio AND h.horaFin")
    Optional<Horario> findTurnoActual(
        @Param("comedorId") Long comedorId, 
        @Param("horaActual") LocalTime horaActual
    );
}