package com.services.comedor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 👩‍💼 ENTIDAD: USUARIO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Representa al PERSONAL OPERATIVO que utiliza las tablets del sistema:
 *   - Cajeros (validan QRs, entregan pedidos)
 *   - Cocineros (ven pedidos, marcan PREPARANDO/LISTO)
 *   - Jefes de comedor (reportes, forzar consumos)
 *   - Administradores (configuración completa)
 * 
 * ================================================================================================================
 * DIFERENCIA ENTRE USUARIO Y EMPLEADO
 * ================================================================================================================
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 * │  USUARIO (staff del sistema)              │  EMPLEADO (comensal)                                          │
 * ├───────────────────────────────────────────┼─────────────────────────────────────────────────────────────────┤
 * │  - Opera el sistema (caja, cocina)        │  - Come en el comedor                                          │
 * │  - Login con username + PIN + comedor     │  - Login con número de empleado + PIN                          │
 * │  - Tiene roles (CAJERO, COCINA, ADMIN)    │  - Tiene permisos de consumo (DESAYUNO, COMIDA, etc.)          │
 * │  - Puede operar en múltiples comedores    │  - Tiene un comedor base                                       │
 * └─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ================================================================================================================
 * ROLES DISPONIBLES
 * ================================================================================================================
 * 
 * | Rol                    | Permisos                                                                 |
 * |------------------------|--------------------------------------------------------------------------|
 * | ROLE_CAJERO            | Escanear QR, entregar pedidos, cancelar pedidos, forzar consumo          |
 * | ROLE_COCINA            | Ver pedidos pendientes, marcar PREPARANDO, marcar LISTO                   |
 * | ROLE_JEFE_COMEDOR      | Reportes, forzar consumo, gestión de su comedor                           |
 * | ROLE_ADMIN             | Acceso total a todos los comedores, gestión de usuarios y empleados       |
 * 
 * ================================================================================================================
 * EJEMPLO DE DATOS
 * ================================================================================================================
 * 
 * | id | username      | nombre          | rol                | comedor_base_id | activo |
 * |----|---------------|-----------------|--------------------|-----------------|--------|
 * | 1  | cajero_norte  | Carlos López    | ROLE_CAJERO        | 1 (Norte)       | true   |
 * | 2  | cocina_norte  | María Torres    | ROLE_COCINA        | 1 (Norte)       | true   |
 * | 3  | jefe_norte    | Roberto Sánchez | ROLE_JEFE_COMEDOR  | 1 (Norte)       | true   |
 * | 4  | admin         | Administrador   | ROLE_ADMIN         | 1 (Norte)       | true   |
 * 
 * ================================================================================================================
 * COMEDORES PERMITIDOS
 * ================================================================================================================
 * 
 * Un usuario puede operar en MÚLTIPLES comedores (ej: un jefe que supervisa Norte y Sur).
 * La tabla intermedia `usuario_comedores` registra estos permisos.
 * 
 * Ejemplo para jefe_norte:
 *   usuario_comedores: (usuario_id=3, comedor_id=1) → puede operar en Norte
 *                     (usuario_id=3, comedor_id=2) → puede operar en Sur
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    
    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // DATOS DE ACCESO
    // =====================================================
    
    /**
     * 🔑 NOMBRE DE USUARIO para login
     * Único en el sistema
     * Ejemplos: "cajero_norte", "cocina_sur", "admin"
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 👤 NOMBRE COMPLETO del usuario
     */
    @Column(length = 100)
    private String nombre;
    
    /**
     * 🔐 PIN de acceso (encriptado con BCrypt)
     * El usuario usa este PIN + username para loguearse en la tablet
     */
    @Column(length = 60)
    private String pin;
    
    // =====================================================
    // ROL Y PERMISOS
    // =====================================================
    
    /**
     * 🎭 ROL del usuario en el sistema
     * Determina qué pantallas y acciones puede realizar:
     * - ROLE_CAJERO: caja
     * - ROLE_COCINA: cocina (KDS)
     * - ROLE_JEFE_COMEDOR: reportes y gestión
     * - ROLE_ADMIN: configuración completa
     */
    @Column(nullable = false, length = 20)
    private String rol;
    
    // =====================================================
    // COMEDORES
    // =====================================================
    
    /**
     * 🏭 COMEDOR BASE del usuario
     * - Es su comedor principal (donde trabaja normalmente)
     * - Se usa para reportes y configuración por defecto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_base_id", nullable = false)
    private Comedor comedorBase;
    
    /**
     * 🏢 COMEDORES PERMITIDOS donde puede operar este usuario
     * Relación Many-to-Many con Comedor
     * 
     * Ejemplos:
     *   - Cajero solo puede operar en su comedor base
     *   - Jefe puede operar en múltiples comedores
     *   - Admin puede operar en TODOS
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_comedores",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "comedor_id")
    )
    @Builder.Default
    private Set<Comedor> comedoresPermitidos = new HashSet<>();
    
    // =====================================================
    // ESTADO
    // =====================================================
    
    /**
     * 🟢 ESTADO ACTIVO del usuario
     * - true: puede iniciar sesión
     * - false: bloqueado (baja, suspensión)
     */
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean activo = true;
}