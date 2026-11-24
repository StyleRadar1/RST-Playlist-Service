package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.exception.AccesoDenegadoPlaylistException;
import edu.dosw.styleradar.playlist.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.playlist.domain.port.in.*;
import edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc.PlaylistApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/playlists")
public class PlaylistController implements PlaylistApi {

    private static final Set<String> ROLES_ADMIN_STYLERADAR = Set.of(
            "STYLERADAR_ADMIN",
            "ADMIN_STYLERADAR",
            "ROLE_STYLERADAR_ADMIN",
            "ROLE_ADMIN_STYLERADAR"
    );

    private final CrearPlaylistUseCase crearPlaylistUseCase;
    private final ConsultarPlaylistUseCase consultarPlaylistUseCase;
    private final DestacarPlaylistUseCase destacarPlaylistUseCase;
    private final AsociarPrendaUseCase asociarPrendaUseCase;
    private final InteractuarPlaylistUseCase interactuarPlaylistUseCase;

    public PlaylistController(
            CrearPlaylistUseCase crearPlaylistUseCase,
            ConsultarPlaylistUseCase consultarPlaylistUseCase,
            DestacarPlaylistUseCase destacarPlaylistUseCase,
            AsociarPrendaUseCase asociarPrendaUseCase,
            InteractuarPlaylistUseCase interactuarPlaylistUseCase) {
        this.crearPlaylistUseCase = crearPlaylistUseCase;
        this.consultarPlaylistUseCase = consultarPlaylistUseCase;
        this.destacarPlaylistUseCase = destacarPlaylistUseCase;
        this.asociarPrendaUseCase = asociarPrendaUseCase;
        this.interactuarPlaylistUseCase = interactuarPlaylistUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<PlaylistResponse> crear(
            @RequestHeader("X-User-Id") String usuarioId,
            @Valid @RequestBody CrearPlaylistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(crearPlaylistUseCase.crear(validarUsuarioId(usuarioId), request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> obtenerPorId(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String usuarioId) {
        String usuarioNormalizado = (usuarioId == null || usuarioId.isBlank()) ? null : usuarioId.trim();
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPorId(id, usuarioNormalizado));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> obtenerPublicas() {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPublicas());
    }

    @Override
    @GetMapping("/featured")
    public ResponseEntity<List<PlaylistResponse>> obtenerDestacadas() {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerDestacadas());
    }

    @Override
    @GetMapping("/popular")
    public ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPopulares(page, size));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> editar(
            @RequestHeader("X-User-Id") String usuarioId,
            @PathVariable UUID id,
            @Valid @RequestBody EditarPlaylistRequest request) {
        return ResponseEntity.ok(crearPlaylistUseCase.editar(validarUsuarioId(usuarioId), id, request));
    }

    @Override
    @PutMapping("/{id}/featured")
    public ResponseEntity<PlaylistResponse> destacar(
            @RequestHeader("X-User-Id") String usuarioId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles,
            @PathVariable UUID id) {
        String usuario = validarUsuarioId(usuarioId);
        validarRolAdmin(userRole, userRoles);
        return ResponseEntity.ok(destacarPlaylistUseCase.destacar(usuario, id));
    }

    @Override
    @DeleteMapping("/{id}/featured")
    public ResponseEntity<PlaylistResponse> quitarDestacado(
            @RequestHeader("X-User-Id") String usuarioId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles,
            @PathVariable UUID id) {
        String usuario = validarUsuarioId(usuarioId);
        validarRolAdmin(userRole, userRoles);
        return ResponseEntity.ok(destacarPlaylistUseCase.quitarDestacado(usuario, id));
    }

    @Override
    @PostMapping("/{id}/items")
    public ResponseEntity<PlaylistResponse> agregarPrenda(
            @RequestHeader("X-User-Id") String usuarioId,
            @PathVariable UUID id,
            @Valid @RequestBody AgregarPrendaRequest request) {
        return ResponseEntity.ok(asociarPrendaUseCase.agregarPrenda(validarUsuarioId(usuarioId), id, request));
    }

    @Override
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<PlaylistResponse> retirarPrenda(
            @RequestHeader("X-User-Id") String usuarioId,
            @PathVariable UUID id,
            @PathVariable("itemId") UUID prendaId) {
        return ResponseEntity.ok(asociarPrendaUseCase.retirarPrenda(validarUsuarioId(usuarioId), id, prendaId));
    }

    @Override
    @PostMapping("/{id}/like")
    public ResponseEntity<PlaylistResponse> alternarLike(
            @RequestHeader("X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(interactuarPlaylistUseCase.alternarLike(validarUsuarioId(usuarioId), id));
    }

    @Override
    @PostMapping("/{id}/save")
    public ResponseEntity<PlaylistResponse> alternarGuardado(
            @RequestHeader("X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(interactuarPlaylistUseCase.alternarGuardado(validarUsuarioId(usuarioId), id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader("X-User-Id") String usuarioId,
            @PathVariable UUID id) {
        crearPlaylistUseCase.eliminar(validarUsuarioId(usuarioId), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> misPlaylists(
            @RequestHeader("X-User-Id") String usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPorUsuario(validarUsuarioId(usuarioId)));
    }

    @Override
    @GetMapping("/me/saved")
    public ResponseEntity<List<PlaylistResponse>> misGuardadas(
            @RequestHeader("X-User-Id") String usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerGuardadas(validarUsuarioId(usuarioId)));
    }

    private String validarUsuarioId(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new NoAutorizadoException("La cabecera X-User-Id es obligatoria y no puede estar vacía");
        }
        return usuarioId.trim();
    }

    private void validarRolAdmin(String userRole, String userRoles) {
        if (tieneRolAdmin(userRole) || tieneRolAdmin(userRoles)) {
            return;
        }
        throw new AccesoDenegadoPlaylistException(
                "Solo un administrador de StyleRadar puede destacar o quitar destacado de una playlist");
    }

    private boolean tieneRolAdmin(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return false;
        }
        String limpio = headerValue.replace("[", "").replace("]", "").replace("\"", "");
        for (String token : limpio.split(",")) {
            if (ROLES_ADMIN_STYLERADAR.contains(token.trim().toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
