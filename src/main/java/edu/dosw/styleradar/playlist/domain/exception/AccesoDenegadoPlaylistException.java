package edu.dosw.styleradar.playlist.domain.exception;

public class AccesoDenegadoPlaylistException extends DomainException {

    public AccesoDenegadoPlaylistException(String message) {
        super(CodigoError.ACCESO_DENEGADO, message);
    }

    protected AccesoDenegadoPlaylistException(CodigoError codigoError, String message) {
        super(codigoError, message);
    }
}
