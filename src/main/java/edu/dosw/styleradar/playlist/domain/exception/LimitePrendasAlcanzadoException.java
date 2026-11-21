package edu.dosw.styleradar.playlist.domain.exception;

public class LimitePrendasAlcanzadoException extends DomainException {

    public LimitePrendasAlcanzadoException(String message) {
        super(CodigoError.LIMITE_PRENDAS_PLAYLIST, message);
    }
}
