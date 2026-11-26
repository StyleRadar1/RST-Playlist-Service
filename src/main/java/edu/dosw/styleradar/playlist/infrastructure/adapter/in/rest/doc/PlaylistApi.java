package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc;

import edu.dosw.styleradar.playlist.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Playlists", description = "Fashion playlists and curated garment collections")
public interface PlaylistApi {

    @Operation(summary = "Create a new fashion playlist (requires at least one approved garment · SR-RF-69)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Playlist created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data, no garments, or description too long (DATOS_INVALIDOS / PLAYLIST_SIN_PRENDAS / DESCRIPCION_MUY_LARGA)",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Invalid request\",\"status\":400,\"detail\":\"The playlist must be created with at least one garment (SR-RF-69)\",\"instance\":\"/api/v1/playlists\",\"code\":\"PLAYLIST_SIN_PRENDAS\",\"timestamp\":\"2026-11-20T22:58:00Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Garment not found in ms-item-catalogo (PRENDA_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Garment not approved, duplicated, or limit exceeded (PRENDA_NO_APROBADA / PRENDA_YA_EN_PLAYLIST / LIMITE_PRENDAS_PLAYLIST)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "ms-item-catalogo service unavailable (CATALOGO_NO_DISPONIBLE)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> crear(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            CrearPlaylistRequest request);

    @Operation(summary = "Get playlist by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist found"),
            @ApiResponse(responseCode = "400", description = "Invalid UUID format (PARAMETRO_INVALIDO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Private playlist owned by another user (PLAYLIST_PRIVADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> obtenerPorId(
            UUID id,
            @Parameter(description = "Optional user ID", example = "15") String usuarioId);

    @Operation(summary = "List public playlists")
    ResponseEntity<List<PlaylistResponse>> obtenerPublicas(
            @Parameter(description = "Optional user ID; when present, liked and saved show that user's state", example = "15") String usuarioId);

    @Operation(summary = "List the 3 featured playlists for Home (SR-RF-72)")
    ResponseEntity<List<PlaylistResponse>> obtenerDestacadas(
            @Parameter(description = "Optional user ID; when present, liked and saved show that user's state", example = "15") String usuarioId);

    @Operation(summary = "List popular playlists with pagination")
    ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(int page, int size,
            @Parameter(description = "Optional user ID; when present, liked and saved show that user's state", example = "15") String usuarioId);

    @Operation(summary = "Update an owned playlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or description too long (DATOS_INVALIDOS / DESCRIPCION_MUY_LARGA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> editar(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            UUID id,
            EditarPlaylistRequest request);

    @Operation(summary = "Mark playlist as featured (StyleRadar admin only, max 3 · SR-RF-72)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist marked as featured"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied: requires STYLERADAR_ADMIN or ADMIN_STYLERADAR role (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Maximum of 3 featured playlists reached (LIMITE_DESTACADAS)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> destacar(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            @Parameter(description = "Primary user role", example = "STYLERADAR_ADMIN") String userRole,
            @Parameter(description = "Comma-separated user roles", example = "STYLERADAR_ADMIN") String userRoles,
            UUID id);

    @Operation(summary = "Remove playlist from featured (StyleRadar admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist removed from featured"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied: requires STYLERADAR_ADMIN or ADMIN_STYLERADAR role (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> quitarDestacado(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            @Parameter(description = "Primary user role", example = "STYLERADAR_ADMIN") String userRole,
            @Parameter(description = "Comma-separated user roles", example = "STYLERADAR_ADMIN") String userRoles,
            UUID id);

    @Operation(summary = "Add an approved garment to the playlist (max 15 · RNF-12)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Garment added to playlist"),
            @ApiResponse(responseCode = "400", description = "Invalid request (DATOS_INVALIDOS / PARAMETRO_INVALIDO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist or garment not found in catalog (PLAYLIST_NO_ENCONTRADA / PRENDA_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Garment not approved, duplicated, or limit reached (PRENDA_NO_APROBADA / PRENDA_YA_EN_PLAYLIST / LIMITE_PRENDAS_PLAYLIST)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "ms-item-catalogo service unavailable (CATALOGO_NO_DISPONIBLE)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> agregarPrenda(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            UUID id,
            AgregarPrendaRequest request);

    @Operation(summary = "Remove a garment from the playlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Garment removed from playlist"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist not found (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Garment does not belong to the playlist (PRENDA_NO_ESTA_EN_PLAYLIST)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> retirarPrenda(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            UUID id,
            UUID prendaId);

    @Operation(summary = "Toggle like on a playlist")
    ResponseEntity<PlaylistResponse> alternarLike(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            UUID id);

    @Operation(summary = "Toggle bookmark/save on a playlist")
    ResponseEntity<PlaylistResponse> alternarGuardado(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            UUID id);

    @Operation(summary = "Delete an owned playlist")
    ResponseEntity<Void> eliminar(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            UUID id);

    @Operation(summary = "List playlists created by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User playlists"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<List<PlaylistResponse>> misPlaylists(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            @Parameter(description = "Owner user ID", example = "15") String userId);

    @Operation(summary = "List playlists saved by a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User saved playlists"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated user (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<List<PlaylistResponse>> misGuardadas(
            @Parameter(description = "User ID forwarded by the API Gateway", example = "15") String usuarioId,
            @Parameter(description = "Owner user ID", example = "15") String userId);
}
