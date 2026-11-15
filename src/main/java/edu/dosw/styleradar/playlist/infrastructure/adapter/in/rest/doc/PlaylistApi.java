package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc;

import edu.dosw.styleradar.playlist.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Playlists", description = "Playlists y colecciones temáticas de prendas de vestir")
public interface PlaylistApi {

    @Operation(summary = "Crear nueva playlist de moda")
    @ApiResponse(responseCode = "201", description = "Playlist creada exitosamente")
    ResponseEntity<PlaylistResponse> crear(UUID usuarioId, CrearPlaylistRequest request);

    @Operation(summary = "Consultar playlist por ID")
    ResponseEntity<PlaylistResponse> obtenerPorId(UUID id, UUID usuarioId);

    @Operation(summary = "Consultar playlists públicas")
    ResponseEntity<List<PlaylistResponse>> obtenerPublicas();

    @Operation(summary = "Consultar las 3 playlists destacadas de Home (SR-RF-72)")
    ResponseEntity<List<PlaylistResponse>> obtenerDestacadas();

    @Operation(summary = "Consultar playlists populares paginadas")
    ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(int page, int size);

    @Operation(summary = "Editar playlist propia")
    ResponseEntity<PlaylistResponse> editar(UUID usuarioId, UUID id, EditarPlaylistRequest request);

    @Operation(summary = "Fijar playlist como destacada (máx 3 · SR-RF-72)")
    ResponseEntity<PlaylistResponse> destacar(UUID usuarioId, UUID id);

    @Operation(summary = "Quitar playlist de destacadas")
    ResponseEntity<PlaylistResponse> quitarDestacado(UUID usuarioId, UUID id);

    @Operation(summary = "Asociar prenda a la playlist (máx 15 · RNF-12)")
    ResponseEntity<PlaylistResponse> agregarPrenda(UUID usuarioId, UUID id, AgregarPrendaRequest request);

    @Operation(summary = "Retirar prenda de la playlist")
    ResponseEntity<PlaylistResponse> retirarPrenda(UUID usuarioId, UUID id, UUID prendaId);

    @Operation(summary = "Alternar Me Gusta en la playlist")
    ResponseEntity<PlaylistResponse> alternarLike(UUID usuarioId, UUID id);

    @Operation(summary = "Alternar guardado en perfil")
    ResponseEntity<PlaylistResponse> alternarGuardado(UUID usuarioId, UUID id);

    @Operation(summary = "Eliminar playlist")
    ResponseEntity<Void> eliminar(UUID usuarioId, UUID id);

    @Operation(summary = "Consultar playlists creadas por el usuario")
    ResponseEntity<List<PlaylistResponse>> misPlaylists(UUID usuarioId);

    @Operation(summary = "Consultar playlists guardadas por el usuario")
    ResponseEntity<List<PlaylistResponse>> misGuardadas(UUID usuarioId);
}
