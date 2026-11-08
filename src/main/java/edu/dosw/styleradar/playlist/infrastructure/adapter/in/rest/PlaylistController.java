package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.port.in.PlaylistUseCasePort;
import edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc.PlaylistApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/playlists")
public class PlaylistController implements PlaylistApi {

    private final PlaylistUseCasePort playlistUseCase;

    public PlaylistController(PlaylistUseCasePort playlistUseCase) {
        this.playlistUseCase = playlistUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<PlaylistResponse> crear(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @Valid @RequestBody CrearPlaylistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistUseCase.crear(usuarioId, request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        return ResponseEntity.ok(playlistUseCase.obtenerPorId(id, usuarioId));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> obtenerPublicas() {
        return ResponseEntity.ok(playlistUseCase.obtenerPublicas());
    }

    @Override
    @GetMapping("/destacadas")
    public ResponseEntity<List<PlaylistResponse>> obtenerDestacadas() {
        return ResponseEntity.ok(playlistUseCase.obtenerDestacadas());
    }

    @Override
    @GetMapping("/populares")
    public ResponseEntity<PageResponse<PlaylistResponse>> obtenerPopulares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(playlistUseCase.obtenerPopulares(page, size));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> editar(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id,
            @Valid @RequestBody EditarPlaylistRequest request) {
        return ResponseEntity.ok(playlistUseCase.editar(usuarioId, id, request));
    }

    @Override
    @PutMapping("/{id}/destacar")
    public ResponseEntity<PlaylistResponse> destacar(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(playlistUseCase.destacar(usuarioId, id));
    }

    @Override
    @DeleteMapping("/{id}/destacar")
    public ResponseEntity<PlaylistResponse> quitarDestacado(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(playlistUseCase.quitarDestacado(usuarioId, id));
    }

    @Override
    @PostMapping("/{id}/prendas")
    public ResponseEntity<PlaylistResponse> agregarPrenda(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id,
            @Valid @RequestBody AgregarPrendaRequest request) {
        return ResponseEntity.ok(playlistUseCase.agregarPrenda(usuarioId, id, request));
    }

    @Override
    @DeleteMapping("/{id}/prendas/{prendaId}")
    public ResponseEntity<PlaylistResponse> retirarPrenda(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id,
            @PathVariable Long prendaId) {
        return ResponseEntity.ok(playlistUseCase.retirarPrenda(usuarioId, id, prendaId));
    }

    @Override
    @PostMapping("/{id}/like")
    public ResponseEntity<PlaylistResponse> alternarLike(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(playlistUseCase.alternarLike(usuarioId, id));
    }

    @Override
    @PostMapping("/{id}/guardar")
    public ResponseEntity<PlaylistResponse> alternarGuardado(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id) {
        return ResponseEntity.ok(playlistUseCase.alternarGuardado(usuarioId, id));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId,
            @PathVariable Long id) {
        playlistUseCase.eliminar(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> misPlaylists(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId) {
        return ResponseEntity.ok(playlistUseCase.obtenerPorUsuario(usuarioId));
    }

    @Override
    @GetMapping("/me/guardadas")
    public ResponseEntity<List<PlaylistResponse>> misGuardadas(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long usuarioId) {
        return ResponseEntity.ok(playlistUseCase.obtenerGuardadas(usuarioId));
    }
}
