package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingUserIdHeaderReturns401AndOtherHeaderReturns400() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("missingUserIdHeaderReturns401AndOtherHeaderReturns400"), -1);

        MissingRequestHeaderException exUser = new MissingRequestHeaderException("X-User-Id", parameter);
        assertCodigoYTimestamp(handler.handleMissingHeader(exUser), HttpStatus.UNAUTHORIZED, "USUARIO_NO_AUTENTICADO");

        MissingRequestHeaderException exOther = new MissingRequestHeaderException("X-Other", parameter);
        assertCodigoYTimestamp(handler.handleMissingHeader(exOther), HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO");
    }

    @Test
    void blankUserIdHeaderReturns401() {
        NoAutorizadoException ex = new NoAutorizadoException("Cabecera vacía");
        assertCodigoYTimestamp(handler.handleNoAutorizado(ex), HttpStatus.UNAUTHORIZED, "USUARIO_NO_AUTENTICADO");
    }

    @Test
    void notFoundReturns404() {
        assertCodigoYTimestamp(handler.handleNotFound(new ResourceNotFoundException("No existe")),
                HttpStatus.NOT_FOUND, "PLAYLIST_NO_ENCONTRADA");

        assertCodigoYTimestamp(handler.handleNotFound(new PrendaNoEncontradaEnCatalogoException("No existe prenda")),
                HttpStatus.NOT_FOUND, "PRENDA_NO_ENCONTRADA");
    }

    @Test
    void limiteDestacadasReturns409() {
        assertCodigoYTimestamp(handler.handleLimiteDestacadas(new LimiteDestacadasSuperadoException("Máx 3")),
                HttpStatus.CONFLICT, "LIMITE_DESTACADAS");
    }

    @Test
    void accesoDenegadoYPrivadaReturn403() {
        assertCodigoYTimestamp(handler.handleAccesoDenegado(new AccesoDenegadoPlaylistException("No dueño")),
                HttpStatus.FORBIDDEN, "ACCESO_DENEGADO");

        assertCodigoYTimestamp(handler.handleAccesoDenegado(new PlaylistPrivadaException("Privada")),
                HttpStatus.FORBIDDEN, "PLAYLIST_PRIVADA");
    }

    @Test
    void domainErrorsReturn400O422ConCodigoPropio() {
        assertCodigoYTimestamp(handler.handleSolicitudNegocioInvalida(new PlaylistSinPrendasException("Sin prendas")),
                HttpStatus.BAD_REQUEST, "PLAYLIST_SIN_PRENDAS");

        assertCodigoYTimestamp(handler.handleSolicitudNegocioInvalida(new DescripcionMuyLargaException("Muy larga")),
                HttpStatus.BAD_REQUEST, "DESCRIPCION_MUY_LARGA");

        assertCodigoYTimestamp(handler.handleDomain(new LimitePrendasAlcanzadoException("Max 15")),
                HttpStatus.UNPROCESSABLE_ENTITY, "LIMITE_PRENDAS_PLAYLIST");

        assertCodigoYTimestamp(handler.handleDomain(new PrendaDuplicadaException("Duplicada")),
                HttpStatus.UNPROCESSABLE_ENTITY, "PRENDA_YA_EN_PLAYLIST");

        assertCodigoYTimestamp(handler.handleDomain(new PrendaNoEncontradaEnPlaylistException("No está")),
                HttpStatus.UNPROCESSABLE_ENTITY, "PRENDA_NO_ESTA_EN_PLAYLIST");

        assertCodigoYTimestamp(handler.handleDomain(new PrendaNoAprobadaException("Pendiente")),
                HttpStatus.UNPROCESSABLE_ENTITY, "PRENDA_NO_APROBADA");

        assertCodigoYTimestamp(handler.handleDomain(new DomainException("Regla incumplida")),
                HttpStatus.UNPROCESSABLE_ENTITY, "REGLA_NEGOCIO_INCUMPLIDA");
    }

    @Test
    void catalogoNoDisponibleReturns503() {
        assertCodigoYTimestamp(handler.handleCatalogoNoDisponible(new CatalogoNoDisponibleException("Caído")),
                HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE");
    }

    @Test
    void validationErrorReturns400WithFieldsAndCode() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ProblemDetail detail = handler.handleValidation(ex);
        assertCodigoYTimestamp(detail, HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");
        assertThat(detail.getProperties()).containsEntry("errors", Map.of("nombre", "no debe estar vacío"));

        HttpMessageNotReadableException unreadable = mock(HttpMessageNotReadableException.class);
        assertCodigoYTimestamp(handler.handleUnreadableBody(unreadable), HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");

        var violation = new ConstraintViolationException("Inválido", Set.of());
        assertCodigoYTimestamp(handler.handleConstraintViolation(violation), HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");
    }

    @Test
    void unexpectedErrorReturns500() {
        Exception ex = new IllegalStateException("fallo");
        assertCodigoYTimestamp(handler.handleUnexpected(ex), HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO");
    }

    @Test
    void unknownRouteReturns404() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/");
        ProblemDetail detail = handler.handleRouteNotFound(ex);
        assertCodigoYTimestamp(detail, HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA");
        assertThat(detail.getTitle()).isEqualTo("Ruta no encontrada");
    }

    @Test
    void invalidParamsReturn400ParametroInvalido() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "no-es-uuid", UUID.class, "id", null, null);
        assertCodigoYTimestamp(handler.handleTypeMismatch(ex), HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO");

        var missingParam = new MissingServletRequestParameterException("page", "int");
        assertCodigoYTimestamp(handler.handleMissingParam(missingParam), HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO");
    }

    private void assertCodigoYTimestamp(ProblemDetail detail, HttpStatus status, String code) {
        assertThat(detail.getStatus()).isEqualTo(status.value());
        assertThat(detail.getProperties()).containsEntry("code", code);
        assertThat(detail.getProperties()).containsKey("timestamp");
        assertThat(Instant.parse(detail.getProperties().get("timestamp").toString())).isNotNull();
    }
}
