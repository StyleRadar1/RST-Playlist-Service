package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.in.*;
import edu.dosw.styleradar.playlist.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    @Mock private CrearPlaylistUseCase crearPlaylistUseCase;
    @Mock private ConsultarPlaylistUseCase consultarPlaylistUseCase;
    @Mock private DestacarPlaylistUseCase destacarPlaylistUseCase;
    @Mock private AsociarPrendaUseCase asociarPrendaUseCase;
    @Mock private InteractuarPlaylistUseCase interactuarPlaylistUseCase;

    private PlaylistController controller;
    private MockMvc mockMvc;
    private PlaylistResponse sampleResponse;
    private final String usuarioId = "15";
    private final UUID playlistId = UUID.randomUUID();
    private final UUID prendaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new PlaylistController(crearPlaylistUseCase, consultarPlaylistUseCase, destacarPlaylistUseCase, asociarPrendaUseCase, interactuarPlaylistUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        sampleResponse = new PlaylistResponse(
                playlistId, usuarioId, "Streetwear", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, false,
                Instant.now(), List.of(), 0, 0, 0
        );
    }

    @Test
    void peticionProtegidaSinHeaderOBlandRetorna401() throws Exception {
        mockMvc.perform(get("/playlists/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/playlists/me").header("X-User-Id", "   "))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Urbano\",\"visibilidad\":\"PUBLICA\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPlaylistConUsuarioEnteroDeGatewayRetorna201() throws Exception {
        when(crearPlaylistUseCase.crear(eq("15"), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/playlists")
                        .header("X-User-Id", "15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Streetwear\",\"descripcion\":\"Desc\",\"visibilidad\":\"PUBLICA\",\"estilo\":\"URBANO\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void obtenerPorIdRetorna200() {
        when(consultarPlaylistUseCase.obtenerPorId(playlistId, usuarioId)).thenReturn(sampleResponse);
        ResponseEntity<PlaylistResponse> response = controller.obtenerPorId(playlistId, usuarioId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obtenerPublicasYPopulares() {
        when(consultarPlaylistUseCase.obtenerPublicas()).thenReturn(List.of(sampleResponse));
        when(consultarPlaylistUseCase.obtenerPopulares(0, 20)).thenReturn(new PageResponse<>(List.of(sampleResponse), 0, 20, 1L, 1, true, true));

        assertThat(controller.obtenerPublicas().getBody()).hasSize(1);
        assertThat(controller.obtenerPopulares(0, 20).getBody().content()).hasSize(1);
    }

    @Test
    void obtenerDestacadasRetornaLista() {
        when(consultarPlaylistUseCase.obtenerDestacadas()).thenReturn(List.of(sampleResponse));
        assertThat(controller.obtenerDestacadas().getBody()).hasSize(1);
    }

    @Test
    void editarPlaylistRetorna200() {
        when(crearPlaylistUseCase.editar(eq(usuarioId), eq(playlistId), any())).thenReturn(sampleResponse);
        ResponseEntity<PlaylistResponse> response = controller.editar(usuarioId, playlistId, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.CASUAL));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void destacarYQuitarDestacado() {
        when(destacarPlaylistUseCase.destacar(usuarioId, playlistId)).thenReturn(sampleResponse);
        when(destacarPlaylistUseCase.quitarDestacado(usuarioId, playlistId)).thenReturn(sampleResponse);

        assertThat(controller.destacar(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.quitarDestacado(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void agregarYRetirarPrenda() {
        when(asociarPrendaUseCase.agregarPrenda(eq(usuarioId), eq(playlistId), any())).thenReturn(sampleResponse);
        when(asociarPrendaUseCase.retirarPrenda(usuarioId, playlistId, prendaId)).thenReturn(sampleResponse);

        assertThat(controller.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaId, "Gorra")).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.retirarPrenda(usuarioId, playlistId, prendaId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void alternarLikeYGuardado() {
        when(interactuarPlaylistUseCase.alternarLike(usuarioId, playlistId)).thenReturn(sampleResponse);
        when(interactuarPlaylistUseCase.alternarGuardado(usuarioId, playlistId)).thenReturn(sampleResponse);

        assertThat(controller.alternarLike(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.alternarGuardado(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminarRetorna204() {
        ResponseEntity<Void> response = controller.eliminar(usuarioId, playlistId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(crearPlaylistUseCase).eliminar(usuarioId, playlistId);
    }

    @Test
    void misPlaylistsYMisGuardadas() {
        when(consultarPlaylistUseCase.obtenerPorUsuario(usuarioId)).thenReturn(List.of(sampleResponse));
        when(consultarPlaylistUseCase.obtenerGuardadas(usuarioId)).thenReturn(List.of(sampleResponse));

        assertThat(controller.misPlaylists(usuarioId).getBody()).hasSize(1);
        assertThat(controller.misGuardadas(usuarioId).getBody()).hasSize(1);
    }

    @Test
    void uuidInvalidoEnRutaRetorna400YNo500() throws Exception {
        mockMvc.perform(get("/playlists/no-es-un-uuid"))
                .andExpect(status().isBadRequest());
    }
}
