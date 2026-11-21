package edu.dosw.styleradar.playlist.domain.exception;

public class LimiteDestacadasSuperadoException extends DomainException {

    public LimiteDestacadasSuperadoException(String message) {
        super(CodigoError.LIMITE_DESTACADAS, message);
    }
}
