package edu.dosw.styleradar.playlist.domain.exception;

import java.util.Objects;

public class DomainException extends RuntimeException {

    private final CodigoError codigoError;

    public DomainException(String message) {
        this(CodigoError.REGLA_NEGOCIO_INCUMPLIDA, message);
    }

    public DomainException(CodigoError codigoError, String message) {
        super(message);
        this.codigoError = Objects.requireNonNull(codigoError, "El código de error es obligatorio");
    }

    public DomainException(CodigoError codigoError, String message, Throwable cause) {
        super(message, cause);
        this.codigoError = Objects.requireNonNull(codigoError, "El código de error es obligatorio");
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }
}
