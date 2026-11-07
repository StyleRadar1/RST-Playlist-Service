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
    void obtenerPublicasYPopulares() {
        when(playlistUseCase.obtenerPublicas()).thenReturn(List.of(sampleResponse));
        when(playlistUseCase.obtenerPopulares(0, 20)).thenReturn(new PageResponse<>(List.of(sampleResponse), 0, 20, 1L, 1, true, true));

        assertThat(controller.obtenerPublicas().getBody()).hasSize(1);
        assertThat(controller.obtenerPopulares(0, 20).getBody().content()).hasSize(1);
    }

    @Test
    void obtenerDestacadasRetornaLista() {
        when(playlistUseCase.obtenerDestacadas()).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<PlaylistResponse>> response = controller.obtenerDestacadas();

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void editarPlaylistRetorna200() {
        when(playlistUseCase.editar(eq(10L), eq(1L), any())).thenReturn(sampleResponse);

        ResponseEntity<PlaylistResponse> response = controller.editar(10L, 1L, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.CASUAL));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void destacarYQuitarDestacado() {
        when(playlistUseCase.destacar(10L, 1L)).thenReturn(sampleResponse);
        when(playlistUseCase.quitarDestacado(10L, 1L)).thenReturn(sampleResponse);

        assertThat(controller.destacar(10L, 1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.quitarDestacado(10L, 1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void agregarYRetirarPrenda() {
        when(playlistUseCase.agregarPrenda(eq(10L), eq(1L), any())).thenReturn(sampleResponse);
        when(playlistUseCase.retirarPrenda(10L, 1L, 5L)).thenReturn(sampleResponse);

        assertThat(controller.agregarPrenda(10L, 1L, new AgregarPrendaRequest(5L, "Gorra")).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.retirarPrenda(10L, 1L, 5L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void alternarLikeYGuardado() {
        when(playlistUseCase.alternarLike(10L, 1L)).thenReturn(sampleResponse);
        when(playlistUseCase.alternarGuardado(10L, 1L)).thenReturn(sampleResponse);

        assertThat(controller.alternarLike(10L, 1L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.alternarGuardado(10L, 1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminarRetorna204() {
        ResponseEntity<Void> response = controller.eliminar(10L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(playlistUseCase).eliminar(10L, 1L);
    }

    @Test
    void misPlaylistsYMisGuardadas() {
        when(playlistUseCase.obtenerPorUsuario(10L)).thenReturn(List.of(sampleResponse));
        when(playlistUseCase.obtenerGuardadas(10L)).thenReturn(List.of(sampleResponse));

        assertThat(controller.misPlaylists(10L).getBody()).hasSize(1);
        assertThat(controller.misGuardadas(10L).getBody()).hasSize(1);
    }
}
