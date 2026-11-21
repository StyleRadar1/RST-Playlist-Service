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

@Tag(name = "Playlists", description = "Playlists y colecciones temáticas de prendas de vestir")
public interface PlaylistApi {

    @Operation(summary = "Crear nueva playlist de moda (requiere al menos una prenda aprobada · SR-RF-69)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Playlist creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, sin prendas o descripción muy larga (DATOS_INVALIDOS / PLAYLIST_SIN_PRENDAS / DESCRIPCION_MUY_LARGA)",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Solicitud inválida\",\"status\":400,\"detail\":\"La playlist debe crearse con al menos una prenda (SR-RF-69)\",\"instance\":\"/api/v1/playlists\",\"code\":\"PLAYLIST_SIN_PRENDAS\",\"timestamp\":\"2026-11-20T22:58:00Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Prenda no existe en ms-item-catalogo (PRENDA_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Prenda no aprobada, duplicada o límite superado (PRENDA_NO_APROBADA / PRENDA_YA_EN_PLAYLIST / LIMITE_PRENDAS_PLAYLIST)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Servicio ms-item-catalogo no disponible (CATALOGO_NO_DISPONIBLE)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> crear(
            @Parameter(description = "UUID del usuario propagado por el Gateway") UUID usuarioId,
            CrearPlaylistRequest request);

    @Operation(summary = "Consultar playlist por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist encontrada"),
            @ApiResponse(responseCode = "400", description = "UUID inválido (PARAMETRO_INVALIDO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Playlist privada de otro usuario (PLAYLIST_PRIVADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> obtenerPorId(
            UUID id,
            @Parameter(description = "UUID de usuario opcional") UUID usuarioId);

    @Operation(summary = "Consultar playlists públicas")
    ResponseEntity<List<PlaylistResponse>> obtenerPublicas();

    @Operation(summary = "Consultar las 3 playlists destacadas de Home (SR-RF-72)")
    ResponseEntity<List<PlaylistResponse>> obtenerDestacadas();

    @Operation(summary = "Consultar playlists populares paginadas")
    ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(int page, int size);

    @Operation(summary = "Editar playlist propia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o descripción muy larga (DATOS_INVALIDOS / DESCRIPCION_MUY_LARGA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> editar(UUID usuarioId, UUID id, EditarPlaylistRequest request);

    @Operation(summary = "Fijar playlist como destacada (máx 3 · SR-RF-72)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist destacada"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Límite de 3 destacadas superado (LIMITE_DESTACADAS)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> destacar(UUID usuarioId, UUID id);

    @Operation(summary = "Quitar playlist de destacadas")
    ResponseEntity<PlaylistResponse> quitarDestacado(UUID usuarioId, UUID id);

    @Operation(summary = "Asociar prenda aprobada a la playlist (máx 15 · RNF-12)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prenda asociada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (DATOS_INVALIDOS / PARAMETRO_INVALIDO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist o prenda no encontrada en catálogo (PLAYLIST_NO_ENCONTRADA / PRENDA_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Prenda no aprobada, repetida o límite alcanzado (PRENDA_NO_APROBADA / PRENDA_YA_EN_PLAYLIST / LIMITE_PRENDAS_PLAYLIST)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Servicio ms-item-catalogo no disponible (CATALOGO_NO_DISPONIBLE)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PlaylistResponse> agregarPrenda(UUID usuarioId, UUID id, AgregarPrendaRequest request);

    @Operation(summary = "Retirar prenda de la playlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prenda retirada"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado (USUARIO_NO_AUTENTICADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (ACCESO_DENEGADO)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada (PLAYLIST_NO_ENCONTRADA)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "La prenda no pertenece a la playlist (PRENDA_NO_ESTA_EN_PLAYLIST)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
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
