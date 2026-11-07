package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.port.in.PlaylistUseCasePort;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    @Mock
    private PlaylistUseCasePort playlistUseCase;

    private PlaylistController controller;

    private PlaylistResponse sampleResponse;

    @BeforeEach
    void setUp() {
        controller = new PlaylistController(playlistUseCase);
        sampleResponse = new PlaylistResponse(
                1L, 10L, "Streetwear", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, false,
                Instant.now(), List.of(), 0, 0, 0
        );
    }

    @Test
    void crearPlaylistRetorna201() {
        when(playlistUseCase.crear(eq(10L), any())).thenReturn(sampleResponse);

        ResponseEntity<PlaylistResponse> response = controller.crear(10L, new CrearPlaylistRequest("Streetwear", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    void obtenerPorIdRetorna200() {
        when(playlistUseCase.obtenerPorId(1L, 10L)).thenReturn(sampleResponse);

        ResponseEntity<PlaylistResponse> response = controller.obtenerPorId(1L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obtenerDestacadasRetornaLista() {
        when(playlistUseCase.obtenerDestacadas()).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<PlaylistResponse>> response = controller.obtenerDestacadas();

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void agregarPrendaRetorna200() {
        when(playlistUseCase.agregarPrenda(eq(10L), eq(1L), any())).thenReturn(sampleResponse);

        ResponseEntity<PlaylistResponse> response = controller.agregarPrenda(10L, 1L, new AgregarPrendaRequest(5L, "Gorra"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminarRetorna204() {
        ResponseEntity<Void> response = controller.eliminar(10L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(playlistUseCase).eliminar(10L, 1L);
    }
}
