package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;

// 2. EMPLEADOS (Los comensales que usan la App / Web)
@Entity
@Table(name = "empleados")
@Getter @Setter // 🔥 Evitamos @Data para no romper JPA
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sigue siendo vital para cruzar datos con Recursos Humanos (Nómina)
    @Column(name = "numero_empleado", nullable = false, unique = true, length = 20)
    private String numeroEmpleado;

    @Column(nullable = false, length = 100)
    private String nombre;

    // unique = true asegura que dos empleados no registren el mismo número
    @Column(unique = true, length = 15)
    private String telefono;

    // length = 60 para encriptar el PIN con BCrypt (Spring Security)
    @Column(length = 60)
    private String pin;

    // 🔥 Agregado: ¿De qué planta/comedor es este empleado para RH?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;

    @Column(columnDefinition = "boolean default true")
    @Builder.Default // Para que el Builder respete el true por defecto
    private Boolean activo = true;
}