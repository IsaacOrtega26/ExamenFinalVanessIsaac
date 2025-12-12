-- Creación de la tabla records
CREATE TABLE IF NOT EXISTS records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    photoPath TEXT,
    date INTEGER NOT NULL,
    weather TEXT
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_date ON records(date);
CREATE INDEX idx_location ON records(latitude, longitude);

-- Consulta para verificar datos
SELECT COUNT(*) as total_records FROM records;

-- Consulta para obtener todos los registros ordenados por fecha
SELECT id, title, latitude, longitude, date FROM records ORDER BY date DESC;

