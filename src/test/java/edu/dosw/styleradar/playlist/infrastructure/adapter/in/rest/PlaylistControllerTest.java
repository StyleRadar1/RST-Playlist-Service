package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.playlist.domain.exception.PlaylistPrivadaException;
import edu.dosw.styleradar.playlist.domain.exception.PlaylistSinPrendasException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoEncontradaEnCatalogoException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                Instant.now(), List.of(), 0, 0, 0, false, false
        );
    }

    @Test
    void peticionProtegidaSinHeaderOBlankRetorna401ConCodigo() throws Exception {
        mockMvc.perform(get("/playlists/users/{userId}", usuarioId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("USUARIO_NO_AUTENTICADO"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        mockMvc.perform(get("/playlists/users/{userId}", usuarioId).header("X-User-Id", "   "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("USUARIO_NO_AUTENTICADO"));

        mockMvc.perform(post("/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Urbano\",\"visibilidad\":\"PUBLICA\",\"prendasIds\":[\"" + prendaId + "\"]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("USUARIO_NO_AUTENTICADO"));
    }

    @Test
    void crearPlaylistConUsuarioEnteroDeGatewayYPrendaRetorna201() throws Exception {
        when(crearPlaylistUseCase.crear(eq("15"), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/playlists")
                        .header("X-User-Id", "15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Streetwear\",\"descripcion\":\"Desc\",\"visibilidad\":\"PUBLICA\",\"estilo\":\"URBANO\",\"prendasIds\":[\"" + prendaId + "\"]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void crearPlaylistSinPrendasRetorna400ConCodigo() throws Exception {
        when(crearPlaylistUseCase.crear(eq("15"), any()))
                .thenThrow(new PlaylistSinPrendasException("Debe incluir al menos una prenda"));

        mockMvc.perform(post("/playlists")
                        .header("X-User-Id", "15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Streetwear\",\"descripcion\":\"Desc\",\"visibilidad\":\"PUBLICA\",\"estilo\":\"URBANO\",\"prendasIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLAYLIST_SIN_PRENDAS"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void agregarPrendaInexistenteOCatalogoCaidoRetorna404Y503() throws Exception {
        when(asociarPrendaUseCase.agregarPrenda(eq("15"), eq(playlistId), any()))
                .thenThrow(new PrendaNoEncontradaEnCatalogoException("Prenda no existe"));

        mockMvc.perform(post("/playlists/{id}/garments", playlistId)
                        .header("X-User-Id", "15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prendaId\":\"" + prendaId + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRENDA_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        when(asociarPrendaUseCase.agregarPrenda(eq("15"), eq(playlistId), any()))
                .thenThrow(new CatalogoNoDisponibleException("Catálogo caído"));

        mockMvc.perform(post("/playlists/{id}/garments", playlistId)
                        .header("X-User-Id", "15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prendaId\":\"" + prendaId + "\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("CATALOGO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void playlistPrivadaRetorna403ConCodigoPlaylistPrivada() throws Exception {
        when(consultarPlaylistUseCase.obtenerPorId(playlistId, "99"))
                .thenThrow(new PlaylistPrivadaException("Privada"));

        mockMvc.perform(get("/playlists/{id}", playlistId).header("X-User-Id", "99"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PLAYLIST_PRIVADA"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void obtenerPorIdRetorna200() {
        when(consultarPlaylistUseCase.obtenerPorId(playlistId, usuarioId)).thenReturn(sampleResponse);
        ResponseEntity<PlaylistResponse> response = controller.obtenerPorId(playlistId, usuarioId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void obtenerPublicasYPopulares() throws Exception {
        when(consultarPlaylistUseCase.obtenerPublicas(null)).thenReturn(List.of(sampleResponse));
        when(consultarPlaylistUseCase.obtenerPopulares(0, 20, null)).thenReturn(new PageResponse<>(List.of(sampleResponse), 0, 20, 1L, 1, true, true));

        assertThat(controller.obtenerPublicas(null).getBody()).hasSize(1);
        assertThat(controller.obtenerPopulares(0, 20, null).getBody().content()).hasSize(1);

        mockMvc.perform(get("/playlists/popular")).andExpect(status().isOk());
    }

    @Test
    void listaConElEstadoDelUsuarioCuandoLlegaElHeader() throws Exception {
        PlaylistResponse conEstado = new PlaylistResponse(
                playlistId, usuarioId, "Streetwear", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, false,
                Instant.now(), List.of(), 1, 1, 0, true, true);
        when(consultarPlaylistUseCase.obtenerPublicas("20")).thenReturn(List.of(conEstado));
        when(consultarPlaylistUseCase.obtenerDestacadas("20")).thenReturn(List.of(conEstado));
        when(consultarPlaylistUseCase.obtenerPopulares(0, 20, "20"))
                .thenReturn(new PageResponse<>(List.of(conEstado), 0, 20, 1L, 1, true, true));

        mockMvc.perform(get("/playlists").header("X-User-Id", " 20 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].liked").value(true))
                .andExpect(jsonPath("$[0].saved").value(true));
        mockMvc.perform(get("/playlists/featured").header("X-User-Id", "20"))
                .andExpect(jsonPath("$[0].liked").value(true));
        mockMvc.perform(get("/playlists/popular").header("X-User-Id", "20"))
                .andExpect(jsonPath("$.content[0].saved").value(true));
    }

    @Test
    void obtenerDestacadasRetornaLista() throws Exception {
        when(consultarPlaylistUseCase.obtenerDestacadas(null)).thenReturn(List.of(sampleResponse));
        assertThat(controller.obtenerDestacadas(null).getBody()).hasSize(1);

        mockMvc.perform(get("/playlists/featured")).andExpect(status().isOk());
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

        assertThat(controller.destacar(usuarioId, "STYLERADAR_ADMIN", null, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.quitarDestacado(usuarioId, null, "CLIENTE, ADMIN_STYLERADAR", playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void destacarYQuitarDestacadoValidaRolAdminRetorna200O403() throws Exception {
        when(destacarPlaylistUseCase.destacar(usuarioId, playlistId)).thenReturn(sampleResponse);
        when(destacarPlaylistUseCase.quitarDestacado(usuarioId, playlistId)).thenReturn(sampleResponse);

        mockMvc.perform(put("/playlists/{id}/featured", playlistId)
                        .header("X-User-Id", usuarioId)
                        .header("X-User-Role", "STYLERADAR_ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/playlists/{id}/featured", playlistId)
                        .header("X-User-Id", usuarioId)
                        .header("X-User-Role", "   ")
                        .header("X-User-Roles", "[\"ROLE_STYLERADAR_ADMIN\"]"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/playlists/{id}/featured", playlistId)
                        .header("X-User-Id", usuarioId)
                        .header("X-User-Role", "CLIENTE"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));

        mockMvc.perform(delete("/playlists/{id}/featured", playlistId)
                        .header("X-User-Id", usuarioId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
    }

    @Test
    void agregarYRetirarPrenda() throws Exception {
        when(asociarPrendaUseCase.agregarPrenda(eq(usuarioId), eq(playlistId), any())).thenReturn(sampleResponse);
        when(asociarPrendaUseCase.retirarPrenda(usuarioId, playlistId, prendaId)).thenReturn(sampleResponse);

        assertThat(controller.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaId, "Gorra")).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.retirarPrenda(usuarioId, playlistId, prendaId).getStatusCode()).isEqualTo(HttpStatus.OK);

        mockMvc.perform(delete("/playlists/{id}/garments/{garmentId}", playlistId, prendaId)
                        .header("X-User-Id", usuarioId))
                .andExpect(status().isOk());
    }

    @Test
    void alternarLikeYGuardado() throws Exception {
        when(interactuarPlaylistUseCase.alternarLike(usuarioId, playlistId)).thenReturn(sampleResponse);
        when(interactuarPlaylistUseCase.alternarGuardado(usuarioId, playlistId)).thenReturn(sampleResponse);

        assertThat(controller.alternarLike(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.alternarGuardado(usuarioId, playlistId).getStatusCode()).isEqualTo(HttpStatus.OK);

        mockMvc.perform(post("/playlists/{id}/likes", playlistId).header("X-User-Id", usuarioId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/playlists/{id}/saves", playlistId).header("X-User-Id", usuarioId))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarRetorna204() {
        ResponseEntity<Void> response = controller.eliminar(usuarioId, playlistId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(crearPlaylistUseCase).eliminar(usuarioId, playlistId);
    }

    @Test
    void misPlaylistsYMisGuardadas() throws Exception {
        when(consultarPlaylistUseCase.obtenerPorUsuario(usuarioId)).thenReturn(List.of(sampleResponse));
        when(consultarPlaylistUseCase.obtenerGuardadas(usuarioId)).thenReturn(List.of(sampleResponse));

        assertThat(controller.misPlaylists(usuarioId, usuarioId).getBody()).hasSize(1);
        assertThat(controller.misGuardadas(usuarioId, usuarioId).getBody()).hasSize(1);

        mockMvc.perform(get("/playlists/users/{userId}", usuarioId)
                        .header("X-User-Id", usuarioId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/playlists/users/{userId}/saved", usuarioId)
                        .header("X-User-Id", usuarioId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/playlists/users/{userId}", "99")
                        .header("X-User-Id", usuarioId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));

        mockMvc.perform(get("/playlists/users/{userId}/saved", "99")
                        .header("X-User-Id", usuarioId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESO_DENEGADO"));
    }

    @Test
    void uuidInvalidoEnRutaRetorna400ConParametroInvalido() throws Exception {
        mockMvc.perform(get("/playlists/no-es-un-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARAMETRO_INVALIDO"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
