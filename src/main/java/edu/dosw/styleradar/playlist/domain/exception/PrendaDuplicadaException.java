package edu.dosw.styleradar.playlist.domain.exception;

public class PrendaDuplicadaException extends DomainException {

    public PrendaDuplicadaException(String message) {
        super(CodigoError.PRENDA_YA_EN_PLAYLIST, message);
    }
}
