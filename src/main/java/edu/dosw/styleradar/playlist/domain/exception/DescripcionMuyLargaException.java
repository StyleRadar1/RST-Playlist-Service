package edu.dosw.styleradar.playlist.domain.exception;

public class DescripcionMuyLargaException extends DomainException {

    public DescripcionMuyLargaException(String message) {
        super(CodigoError.DESCRIPCION_MUY_LARGA, message);
    }
}
