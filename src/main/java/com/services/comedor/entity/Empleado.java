package com.services.comedor.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

/**
 * 👤 ENTIDAD: EMPLEADO
 * 
 * ================================================================================================================
 * PROPÓSITO
 * ================================================================================================================
 * 
 * Representa a un COMENSAL (empleado de la empresa) que utiliza el sistema para consumir alimentos.
 * Cada empleado tiene un número único, un PIN personal, y permisos específicos sobre qué tipos de comida
 * puede consumir y en qué horarios.
 * 
 * ================================================================================================================
 * DIFERENCIA ENTRE EMPLEADO Y USUARIO
 * ================================================================================================================
 * 
 * ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 * │  EMPLEADO (comensal)                    │  USUARIO (staff del sistema)                                      │
 * ├─────────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤
 * │  - Quien COME en el comedor             │  - Quien OPERA el sistema (cajero, cocina, admin)                 │
 * │  - Se autentica con número + PIN        │  - Se autentica con username + PIN + comedor                      │
 * │  - Tiene permisos de consumo            │  - Tiene roles (CAJERO, COCINA, ADMIN)                            │
 * │  - Ve menú y genera pedidos             │  - Escanea QR, entrega, prepara                                    │
 * └─────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
 * 
 * ================================================================================================================
 * TIPOS DE EMPLEADOS
 * ================================================================================================================
 * 
 * | Perfil        | horarioFlexible | Permisos típicos          | Descripción                          |
 * |---------------|-----------------|---------------------------|--------------------------------------|
 * | Camionero     | true            | COMIDA, TIENDA            | Horario variable, come cuando puede |
 * | Administrativo| false           | DESAYUNO, COMIDA          | Horario fijo, come en sus horas     |
 * 
 * ================================================================================================================
 * 
 * @author TuNombre
 * @since 1.0
 */
@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {

    // =====================================================
    // IDENTIFICADOR
    // =====================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // =====================================================
    // DATOS PERSONALES
    // =====================================================
    
    /**
     * 🆔 NÚMERO ÚNICO de empleado (para nómina y autenticación)
     * Formato recomendado: CAM001, ADM001, PLA001
     */
    @Column(name = "numero_empleado", nullable = false, unique = true, length = 20)
    private String numeroEmpleado;
    
    /**
     * 👤 NOMBRE completo del empleado
     */
    @Column(nullable = false, length = 100)
    private String nombre;
    
    /**
     * 📞 TELÉFONO del empleado (para login alternativo)
     * Único para evitar duplicados
     */
    @Column(unique = true, length = 15)
    private String telefono;
    
    /**
     * 🔐 PIN de acceso (encriptado con BCrypt)
     * El empleado usa este PIN + número de empleado para loguearse en la app
     */
    @Column(length = 60)
    private String pin;
    
    // =====================================================
    // UBICACIÓN
    // =====================================================
    
    /**
     * 🏭 COMEDOR BASE del empleado
     * - Para administrativos: comedor donde trabaja
     * - Para camioneros: comedor asignado por RRHH (puede comer en otros)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comedor_id", nullable = false)
    private Comedor comedor;
    
    // =====================================================
    // ESTADO
    // =====================================================
    
    /**
     * 🟢 ESTADO ACTIVO del empleado
     * - true: puede generar pedidos
     * - false: bloqueado (baja, suspensión)
     */
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean activo = true;
    
    // =====================================================
    // PERMISOS Y CONFIGURACIÓN
    // =====================================================
    
    /**
     * 🍽️ TIPOS DE CONSUMO PERMITIDOS
     * Define QUÉ puede consumir este empleado:
     * - DESAYUNO (id=1)
     * - COMIDA (id=2)
     * - CENA (id=3)
     * - TIENDA (id=99)
     * 
     * Ejemplos:
     * - Camionero: [COMIDA, TIENDA]
     * - Administrativo: [DESAYUNO, COMIDA]
     * 
     * Relación Many-to-Many con TiposConsumo
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "empleado_permisos_consumo",
            joinColumns = @JoinColumn(name = "empleado_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_consumo_id")
    )
    @Builder.Default
    private Set<TipoConsumo> consumosPermitidos = new HashSet<>();
    
    /**
     * 🕐 HORARIO FLEXIBLE
     * - true: camionero, puede consumir en cualquier horario (solo valida que el comedor esté sirviendo)
     * - false: administrativo, solo puede consumir dentro de su horario laboral
     */
    @Column(name = "horario_flexible", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean horarioFlexible = false;
    
    // =====================================================
    // MÉTODOS DE CONVENIENCIA
    // =====================================================
    
    /**
     * ✅ Verifica si el empleado tiene permiso para consumir un tipo específico
     * 
     * @param tipo Tipo de consumo a verificar
     * @return true si tiene permiso, false si no
     */
    public boolean puedeConsumir(TipoConsumo tipo) {
        return consumosPermitidos != null && consumosPermitidos.contains(tipo);
    }
    
    /**
     * ✅ Verifica si el empleado tiene permiso para consumir un tipo por su ID
     * 
     * @param tipoConsumoId ID del tipo de consumo
     * @return true si tiene permiso, false si no
     */
    public boolean puedeConsumir(Long tipoConsumoId) {
        return consumosPermitidos != null
                && consumosPermitidos.stream().anyMatch(t -> t.getId().equals(tipoConsumoId));
    }
}