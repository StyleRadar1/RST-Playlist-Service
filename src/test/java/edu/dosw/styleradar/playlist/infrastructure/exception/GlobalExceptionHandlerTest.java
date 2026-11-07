package edu.dosw.styleradar.playlist.infrastructure.exception;

import edu.dosw.styleradar.playlist.domain.exception.AccesoDenegadoPlaylistException;
import edu.dosw.styleradar.playlist.domain.exception.DomainException;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("No existe");
        ProblemDetail detail = handler.handleNotFound(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getDetail()).isEqualTo("No existe");
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
        assertThat(detail.getTitle()).isEqualTo("Regla de negocio incumplida");
    }

    @Test
    void validationErrorReturns400WithFields() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "request");
        result.addError(new FieldError("request", "nombre", "no debe estar vacío"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(result);

        ProblemDetail detail = handler.handleValidation(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getProperties()).containsEntry("errors", Map.of("nombre", "no debe estar vacío"));
    }

    @Test
    void unexpectedErrorReturns500() {
        Exception ex = new IllegalStateException("fallo");
        ProblemDetail detail = handler.handleUnexpected(ex);
        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getDetail()).isEqualTo("Ocurrió un error inesperado");
    }
}
