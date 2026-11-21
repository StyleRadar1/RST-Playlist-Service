package edu.dosw.styleradar.playlist.domain.exception;

public class PrendaNoEncontradaEnPlaylistException extends DomainException {

    public PrendaNoEncontradaEnPlaylistException(String message) {
        super(CodigoError.PRENDA_NO_ESTA_EN_PLAYLIST, message);
    }
}
