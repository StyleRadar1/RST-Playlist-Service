package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.port.in.*;
import edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc.PlaylistApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/playlists")
public class PlaylistController implements PlaylistApi {

    private final CrearPlaylistUseCase crearPlaylistUseCase;
    private final ConsultarPlaylistUseCase consultarPlaylistUseCase;
    private final DestacarPlaylistUseCase destacarPlaylistUseCase;
    private final AsociarPrendaUseCase asociarPrendaUseCase;
    private final InteractuarPlaylistUseCase interactuarPlaylistUseCase;

    public PlaylistController(CrearPlaylistUseCase crearPlaylistUseCase,
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
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @Valid @RequestBody CrearPlaylistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crearPlaylistUseCase.crear(usuarioId, request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> obtenerPorId(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPorId(id, usuarioId));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> obtenerPublicas() {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPublicas());
    }

    @Override
    @GetMapping("/destacadas")
    public ResponseEntity<List<PlaylistResponse>> obtenerDestacadas() {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerDestacadas());
    }

    @Override
    @GetMapping("/populares")
    public ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPopulares(page, size));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> editar(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id,
            @Valid @RequestBody EditarPlaylistRequest request) {
        return ResponseEntity.ok(crearPlaylistUseCase.editar(usuarioId, id, request));
    }

    @Override
    @PutMapping("/{id}/destacar")
    public ResponseEntity<PlaylistResponse> destacar(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(destacarPlaylistUseCase.destacar(usuarioId, id));
    }

    @Override
    @DeleteMapping("/{id}/destacar")
    public ResponseEntity<PlaylistResponse> quitarDestacado(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(destacarPlaylistUseCase.quitarDestacado(usuarioId, id));
    }

    @Override
    @PostMapping("/{id}/prendas")
    public ResponseEntity<PlaylistResponse> agregarPrenda(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id,
            @Valid @RequestBody AgregarPrendaRequest request) {
        return ResponseEntity.ok(asociarPrendaUseCase.agregarPrenda(usuarioId, id, request));
    }

    @Override
    @DeleteMapping("/{id}/prendas/{prendaId}")
    public ResponseEntity<PlaylistResponse> retirarPrenda(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id,
            @PathVariable UUID prendaId) {
        return ResponseEntity.ok(asociarPrendaUseCase.retirarPrenda(usuarioId, id, prendaId));
    }

    @Override
    @PostMapping("/{id}/like")
    public ResponseEntity<PlaylistResponse> alternarLike(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(interactuarPlaylistUseCase.alternarLike(usuarioId, id));
    }

    @Override
    @PostMapping("/{id}/guardar")
    public ResponseEntity<PlaylistResponse> alternarGuardado(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(interactuarPlaylistUseCase.alternarGuardado(usuarioId, id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = "X-User-Id") UUID usuarioId,
            @PathVariable UUID id) {
        crearPlaylistUseCase.eliminar(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> misPlaylists(
            @RequestHeader(value = "X-User-Id") UUID usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerPorUsuario(usuarioId));
    }

    @Override
    @GetMapping("/me/guardadas")
    public ResponseEntity<List<PlaylistResponse>> misGuardadas(
            @RequestHeader(value = "X-User-Id") UUID usuarioId) {
        return ResponseEntity.ok(consultarPlaylistUseCase.obtenerGuardadas(usuarioId));
    }
}
