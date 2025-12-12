-- Consultas para verificar datos en la base de datos

-- 1. Ver todos los registros
SELECT * FROM records;

-- 2. Contar total de registros
SELECT COUNT(*) as total FROM records;

-- 3. Ver registros ordenados por fecha (más reciente primero)
SELECT id, title, date, latitude, longitude 
FROM records 
ORDER BY date DESC;

-- 4. Ver registros con fotos
SELECT id, title, photoPath 
FROM records 
WHERE photoPath IS NOT NULL;

-- 5. Ver estadísticas por ubicación
SELECT 
    ROUND(latitude, 2) as lat_group,
    ROUND(longitude, 2) as lng_group,
    COUNT(*) as count
FROM records
GROUP BY lat_group, lng_group
ORDER BY count DESC;

-- 6. Consulta para backup
SELECT * FROM records 
WHERE date >= strftime('%s', 'now', '-30 days');

-- 7. Ver estructura de la tabla
PRAGMA table_info(records);

