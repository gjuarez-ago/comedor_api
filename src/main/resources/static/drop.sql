-- =====================================================
-- DROP ALL TABLES - COMEDOR SYSTEM (POSTGRESQL)
-- =====================================================
-- ⚠️ ADVERTENCIA: Este script eliminará TODAS las tablas
-- ⚠️ y TODOS los datos del sistema.
-- ⚠️ Asegúrate de tener un backup antes de ejecutarlo.
-- =====================================================

-- Deshabilitar verificaciones de claves foráneas temporalmente
-- (PostgreSQL usa diferentes enfoques)

-- =====================================================
-- 1. ELIMINAR TABLAS CON DEPENDENCIAS (orden inverso)
-- =====================================================

-- Tablas de consumo y pedidos
DROP TABLE IF EXISTS consumo_detalle_modificadores CASCADE;
DROP TABLE IF EXISTS consumo_detalle CASCADE;
DROP TABLE IF EXISTS consumo_estado_historial CASCADE;
DROP TABLE IF EXISTS consumos CASCADE;

-- Tablas de inventario y stock
DROP TABLE IF EXISTS inventario_movimientos CASCADE;
DROP TABLE IF EXISTS producto_stock CASCADE;

-- Tablas de productos y menús
DROP TABLE IF EXISTS producto_dias_disponibles CASCADE;
DROP TABLE IF EXISTS comedor_producto_turnos CASCADE;
DROP TABLE IF EXISTS comedor_productos CASCADE;

-- Tablas de usuarios y permisos
DROP TABLE IF EXISTS usuario_comedores CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- Tablas de empleados
DROP TABLE IF EXISTS empleado_permisos_consumo CASCADE;
DROP TABLE IF EXISTS empleados CASCADE;

-- Tablas de horarios y turnos
DROP TABLE IF EXISTS horarios CASCADE;
DROP TABLE IF EXISTS turnos CASCADE;

-- Tablas de modificadores
DROP TABLE IF EXISTS opciones_modificadores CASCADE;
DROP TABLE IF EXISTS grupos_modificadores CASCADE;

-- Tablas base
DROP TABLE IF EXISTS tipos_consumo CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS comedores CASCADE;

-- Tabla de folios
DROP TABLE IF EXISTS registro_folios CASCADE;

-- =====================================================
-- 2. VERIFICAR QUE NO QUEDEN TABLAS
-- =====================================================

-- Listar tablas restantes (debería estar vacío)
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- =====================================================
-- 3. NOTA
-- =====================================================
-- Si usas esquemas específicos, cambia 'public' por tu esquema
-- Ejemplo: WHERE table_schema = 'comedor_schema'