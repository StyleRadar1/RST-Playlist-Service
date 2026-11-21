package edu.dosw.styleradar.playlist.domain.exception;

public class PlaylistSinPrendasException extends DomainException {

    public PlaylistSinPrendasException(String message) {
        super(CodigoError.PLAYLIST_SIN_PRENDAS, message);
    }
}
