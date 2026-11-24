package edu.dosw.styleradar.playlist.domain.exception;

public class PlaylistPrivadaException extends AccesoDenegadoPlaylistException {

    public PlaylistPrivadaException(String message) {
        super(CodigoError.PLAYLIST_PRIVADA, message);
    }
}
