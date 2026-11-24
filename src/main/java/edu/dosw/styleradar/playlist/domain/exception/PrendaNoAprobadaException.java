package edu.dosw.styleradar.playlist.domain.exception;

public class PrendaNoAprobadaException extends DomainException {

    public PrendaNoAprobadaException(String message) {
        super(CodigoError.PRENDA_NO_APROBADA, message);
    }
}
