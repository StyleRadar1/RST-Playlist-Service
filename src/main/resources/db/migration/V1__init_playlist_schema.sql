-- ==============================================================================
-- STYLERADAR · ms-playlist · Migración Inicial V1 (PostgreSQL 16/18)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS playlists (
                                         id BIGSERIAL PRIMARY KEY,
                                         usuario_id BIGINT NOT NULL,
                                         nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    visibilidad VARCHAR(20) NOT NULL,
    estilo VARCHAR(30) NOT NULL,
    destacada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL
                                      );

-- Colección de prendas vinculadas al lookbook (desacoplado sin FK externa)
CREATE TABLE IF NOT EXISTS playlist_prendas (
                                                playlist_id BIGINT NOT NULL,
                                                prenda_id BIGINT NOT NULL,
                                                nombre_prenda VARCHAR(150),
    fecha_asociacion TIMESTAMP WITH TIME ZONE,
                                   CONSTRAINT fk_playlist_prendas FOREIGN KEY (playlist_id)
    REFERENCES playlists (id) ON DELETE CASCADE
    );

-- Likes de usuarios
CREATE TABLE IF NOT EXISTS playlist_likes (
                                              playlist_id BIGINT NOT NULL,
                                              usuario_id BIGINT NOT NULL,
                                              CONSTRAINT pk_playlist_likes PRIMARY KEY (playlist_id, usuario_id),
    CONSTRAINT fk_playlist_likes FOREIGN KEY (playlist_id)
    REFERENCES playlists (id) ON DELETE CASCADE
    );

-- Playlists guardadas en perfil
CREATE TABLE IF NOT EXISTS playlist_guardados (
                                                  playlist_id BIGINT NOT NULL,
                                                  usuario_id BIGINT NOT NULL,
                                                  CONSTRAINT pk_playlist_guardados PRIMARY KEY (playlist_id, usuario_id),
    CONSTRAINT fk_playlist_guardados FOREIGN KEY (playlist_id)
    REFERENCES playlists (id) ON DELETE CASCADE
    );

-- Índices de alto rendimiento
CREATE INDEX IF NOT EXISTS idx_playlists_usuario ON playlists (usuario_id);
CREATE INDEX IF NOT EXISTS idx_playlists_destacada ON playlists (destacada);
CREATE INDEX IF NOT EXISTS idx_playlists_visibilidad ON playlists (visibilidad);
CREATE INDEX IF NOT EXISTS idx_playlist_prendas_prenda ON playlist_prendas (prenda_id);