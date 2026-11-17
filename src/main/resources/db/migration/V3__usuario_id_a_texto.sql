ALTER TABLE playlists
    ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::text;

ALTER TABLE playlist_likes
    ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::text;

ALTER TABLE playlist_guardados
    ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::text;
