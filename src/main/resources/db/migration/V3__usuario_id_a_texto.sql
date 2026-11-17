DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'playlists' AND column_name = 'usuario_id' AND data_type <> 'character varying'
    ) THEN
        ALTER TABLE playlists ALTER COLUMN usuario_id DROP DEFAULT;
        ALTER TABLE playlists ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::varchar(100);
    END IF;
 
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'playlist_likes' AND column_name = 'usuario_id' AND data_type <> 'character varying'
    ) THEN
        ALTER TABLE playlist_likes ALTER COLUMN usuario_id DROP DEFAULT;
        ALTER TABLE playlist_likes ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::varchar(100);
    END IF;
 
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'playlist_guardados' AND column_name = 'usuario_id' AND data_type <> 'character varying'
    ) THEN
        ALTER TABLE playlist_guardados ALTER COLUMN usuario_id DROP DEFAULT;
        ALTER TABLE playlist_guardados ALTER COLUMN usuario_id TYPE VARCHAR(100) USING usuario_id::varchar(100);
    END IF;
END
$$;