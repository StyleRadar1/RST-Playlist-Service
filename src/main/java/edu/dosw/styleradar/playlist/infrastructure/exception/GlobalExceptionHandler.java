package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.AccesoDenegadoPlaylistException;
import edu.dosw.styleradar.playlist.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.playlist.domain.exception.CodigoError;
import edu.dosw.styleradar.playlist.domain.exception.DescripcionMuyLargaException;
import edu.dosw.styleradar.playlist.domain.exception.DomainException;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.playlist.domain.exception.PlaylistSinPrendasException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        if ("X-User-Id".equalsIgnoreCase(ex.getHeaderName())) {
            return problem(HttpStatus.UNAUTHORIZED, CodigoError.USUARIO_NO_AUTENTICADO,
                    "No autorizado", "La cabecera X-User-Id es obligatoria para acceder a este recurso");
        }
        return problem(HttpStatus.BAD_REQUEST, CodigoError.PARAMETRO_INVALIDO,
                "Cabecera faltante", ex.getMessage());
    }

    @ExceptionHandler(NoAutorizadoException.class)
    public ProblemDetail handleNoAutorizado(NoAutorizadoException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getCodigoError(), "No autorizado", ex.getMessage());
    }

    @ExceptionHandler(NoAutorizadoException.class)
    public ProblemDetail handleNoAutorizado(NoAutorizadoException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.PARAMETRO_INVALIDO, "Parámetro inválido",
                "El parámetro '" + ex.getName() + "' tiene un formato inválido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.PARAMETRO_INVALIDO, "Parámetro faltante",
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
    }

    @ExceptionHandler({ResourceNotFoundException.class, PrendaNoEncontradaEnCatalogoException.class})
    public ProblemDetail handleNotFound(DomainException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getCodigoError(), "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleRouteNotFound(Exception ex) {
        return problem(HttpStatus.NOT_FOUND, CodigoError.RUTA_NO_ENCONTRADA,
                "Ruta no encontrada", "El recurso o ruta solicitada no existe");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleRouteNotFound(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Ruta no encontrada", ex.getMessage());
    }

    @ExceptionHandler(LimiteDestacadasSuperadoException.class)
    public ProblemDetail handleLimiteDestacadas(LimiteDestacadasSuperadoException ex) {
        return problem(HttpStatus.CONFLICT, ex.getCodigoError(),
                "Límite de destacadas superado", ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoPlaylistException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoPlaylistException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getCodigoError(), "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler({PlaylistSinPrendasException.class, DescripcionMuyLargaException.class})
    public ProblemDetail handleSolicitudNegocioInvalida(DomainException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getCodigoError(), "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(CatalogoNoDisponibleException.class)
    public ProblemDetail handleCatalogoNoDisponible(CatalogoNoDisponibleException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ex.getCodigoError(),
                "Servicio de catálogo no disponible", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigoError(),
                "Regla de negocio incumplida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Solicitud inválida", "La solicitud tiene campos inválidos");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Cuerpo de solicitud inválido",
                "El formato del JSON o alguno de los valores enviados no es válido");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        return problem(HttpStatus.BAD_REQUEST, CodigoError.DATOS_INVALIDOS,
                "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error interno inesperado en ms-playlist", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, CodigoError.ERROR_INTERNO,
                "Error interno", "Ocurrió un error inesperado");
    }

    private ProblemDetail problem(HttpStatus status, CodigoError code, String title, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setProperty("code", code.name());
        detail.setProperty("timestamp", Instant.now().toString());
        return detail;
    }
}
