package com.services.comedor.repository;

import com.services.comedor.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // 📱 APP EMPLEADO (Comensal)
    // Para iniciar sesión en la app móvil.
    Optional<Empleado> findByTelefonoAndPinAndActivoTrue(String telefono, String pin);

    // 📱 APP EMPLEADO (Comensal)
    // Para iniciar sesión en la app móvil.
    Optional<Empleado> findByTelefono(String telefono);

    // 💻 APP USUARIO (POS/Caja)
    // Por si el empleado olvida el teléfono y el cajero digita su número de nómina.
    Optional<Empleado> findByNumeroEmpleadoAndActivoTrue(String numeroEmpleado);
}