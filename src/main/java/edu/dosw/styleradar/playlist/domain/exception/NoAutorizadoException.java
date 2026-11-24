package edu.dosw.styleradar.playlist.domain.exception;

public class NoAutorizadoException extends DomainException {

    public NoAutorizadoException(String message) {
        super(CodigoError.USUARIO_NO_AUTENTICADO, message);
    }
}
