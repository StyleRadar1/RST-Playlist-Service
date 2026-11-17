package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingRequestHeaderException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingUserIdHeaderReturns401() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(this.getClass().getDeclaredMethod("missingUserIdHeaderReturns401"), -1);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-User-Id", parameter);
        ProblemDetail detail = handler.handleMissingHeader(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void blankUserIdHeaderReturns401() {
        NoAutorizadoException ex = new NoAutorizadoException("Cabecera vacía");
        ProblemDetail detail = handler.handleNoAutorizado(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void notFoundReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("No existe");
        ProblemDetail detail = handler.handleNotFound(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void limiteDestacadasReturns409() {
        LimiteDestacadasSuperadoException ex = new LimiteDestacadasSuperadoException("Máx 3");
        ProblemDetail detail = handler.handleLimiteDestacadas(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void accesoDenegadoReturns403() {
        AccesoDenegadoPlaylistException ex = new AccesoDenegadoPlaylistException("Privada");
        ProblemDetail detail = handler.handleAccesoDenegado(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void domainErrorReturns422() {
        DomainException ex = new DomainException("Regla incumplida");
        ProblemDetail detail = handler.handleDomain(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void validationErrorReturns400WithFields() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ProblemDetail detail = handler.handleValidation(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void unexpectedErrorReturns500() {
        Exception ex = new IllegalStateException("fallo");
        ProblemDetail detail = handler.handleUnexpected(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void unknownRouteReturns404() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/");
        ProblemDetail detail = handler.handleRouteNotFound(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getTitle()).isEqualTo("Ruta no encontrada");
    }
}
