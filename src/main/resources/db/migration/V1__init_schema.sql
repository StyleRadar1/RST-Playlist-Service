CREATE TABLE IF NOT EXISTS playlists (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    visibilidad VARCHAR(20) NOT NULL DEFAULT 'PUBLICA',
    estilo VARCHAR(30) NOT NULL DEFAULT 'URBANO',
    destacada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS playlist_prendas (
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    prenda_id UUID NOT NULL,
    nombre_prenda VARCHAR(150),
    fecha_asociacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS playlist_likes (
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL,
    PRIMARY KEY (playlist_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS playlist_guardados (
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL,
    PRIMARY KEY (playlist_id, usuario_id)
);
