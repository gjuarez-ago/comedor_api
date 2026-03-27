package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

// 3. USUARIOS (TABLETS / LOGIN)
@Entity
@Table(name = "usuarios")
@Getter @Setter // 🔥 Evitamos @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Agregado: Necesario para el LoginUsuarioRequest (su ID, gafete o correo)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 100)
    private String nombre;

    // length = 60 para usar BCrypt
    @Column(length = 60)
    private String pin;

    @Column(nullable = false, length = 20)
    private String rol; // ROLE_CAJERO, ROLE_COCINA, ROLE_JEFE_COMEDOR, ROLE_ADMIN

    // 🔥 NUEVO: Su casa (Para el Dashboard y reportes)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_base_id", nullable = false)
    private Comedor comedorBase;

    // EL CAMBIO ESTRELLA: Múltiples comedores por usuario (Sus Visas)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_comedores",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "comedor_id")
    )
    @Builder.Default // Inicializamos para evitar NullPointerException
    private Set<Comedor> comedoresPermitidos = new HashSet<>();

    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean activo = true;
}