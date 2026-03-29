-- =====================================================
-- SISTEMA DE COMEDOR - CARGA INICIAL
-- =====================================================
-- 7 Comedores
-- Operación: Lunes a Sábado (Domingo cerrado)
-- Tipos: DESAYUNO, COMIDA, CENA, TIENDA
-- =====================================================

-- 1. COMEDORES
INSERT INTO comedores (id, nombre, activo) VALUES
(1, 'COMEDOR NORTE', true),
(2, 'COMEDOR SUR', true),
(3, 'COMEDOR CENTRAL', true),
(4, 'COMEDOR BAJÍO', true),
(5, 'COMEDOR OCCIDENTE', true),
(6, 'COMEDOR GOLFO', true),
(7, 'COMEDOR PENÍNSULA', true)
ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre;

-- 2. TIPOS DE CONSUMO
INSERT INTO tipos_consumo (id, nombre) VALUES
(1, 'DESAYUNO'),
(2, 'COMIDA'),
(3, 'CENA'),
(99, 'TIENDA')
ON CONFLICT (id) DO NOTHING;

-- 3. HORARIOS (Lunes a Sábado)
DO $$
DECLARE
    comedor RECORD;
    dia INT;
BEGIN
    FOR comedor IN SELECT id FROM comedores LOOP
        FOR dia IN 1..6 LOOP  -- Lunes a Sábado
            -- Desayuno: 7:00 - 9:00
            INSERT INTO horarios (comedor_id, tipo_consumo_id, dia_semana, hora_inicio, hora_fin, activo)
            VALUES (comedor.id, 1, dia, '07:00:00', '09:00:00', true)
            ON CONFLICT (comedor_id, tipo_consumo_id, dia_semana) DO NOTHING;
            
            -- Comida: 12:00 - 15:00
            INSERT INTO horarios (comedor_id, tipo_consumo_id, dia_semana, hora_inicio, hora_fin, activo)
            VALUES (comedor.id, 2, dia, '12:00:00', '15:00:00', true)
            ON CONFLICT (comedor_id, tipo_consumo_id, dia_semana) DO NOTHING;
            
            -- Cena: 18:00 - 20:00
            INSERT INTO horarios (comedor_id, tipo_consumo_id, dia_semana, hora_inicio, hora_fin, activo)
            VALUES (comedor.id, 3, dia, '18:00:00', '20:00:00', true)
            ON CONFLICT (comedor_id, tipo_consumo_id, dia_semana) DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- 4. VERIFICACIÓN FINAL
SELECT 
    'RESUMEN DE CARGA' AS seccion,
    (SELECT COUNT(*) FROM comedores) AS total_comedores,
    (SELECT COUNT(*) FROM tipos_consumo) AS total_tipos,
    (SELECT COUNT(*) FROM horarios) AS total_horarios;

-- 5. DETALLE DE HORARIOS
SELECT 
    c.nombre AS comedor,
    CASE h.dia_semana
        WHEN 1 THEN 'LUNES'
        WHEN 2 THEN 'MARTES'
        WHEN 3 THEN 'MIÉRCOLES'
        WHEN 4 THEN 'JUEVES'
        WHEN 5 THEN 'VIERNES'
        WHEN 6 THEN 'SÁBADO'
    END AS dia,
    tc.nombre AS tipo,
    h.hora_inicio,
    h.hora_fin
FROM horarios h
JOIN comedores c ON c.id = h.comedor_id
JOIN tipos_consumo tc ON tc.id = h.tipo_consumo_id
ORDER BY c.id, h.dia_semana, h.tipo_consumo_id;