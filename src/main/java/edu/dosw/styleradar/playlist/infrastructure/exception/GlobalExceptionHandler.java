package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.AccesoDenegadoPlaylistException;
import edu.dosw.styleradar.playlist.domain.exception.DomainException;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        if ("X-User-Id".equalsIgnoreCase(ex.getHeaderName())) {
            return problem(HttpStatus.UNAUTHORIZED, "No autorizado", "La cabecera X-User-Id es obligatoria para acceder a este recurso");
        }
        return problem(HttpStatus.BAD_REQUEST, "Cabecera faltante", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("X-User-Id".equalsIgnoreCase(ex.getName())) {
            return problem(HttpStatus.UNAUTHORIZED, "No autorizado", "La cabecera X-User-Id debe ser un UUID válido");
        }
        return problem(HttpStatus.BAD_REQUEST, "Parámetro inválido", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(LimiteDestacadasSuperadoException.class)
    public ProblemDetail handleLimiteDestacadas(LimiteDestacadasSuperadoException ex) {
        return problem(HttpStatus.CONFLICT, "Límite de destacadas superado", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoPlaylistException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoPlaylistException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", "La solicitud tiene campos inválidos");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado");
    }

    private ProblemDetail problem(HttpStatus status, String title, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        return detail;
    }
}
