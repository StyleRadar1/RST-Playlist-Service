package edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.domain.exception.UnauthorizedException;
import edu.dosw.styleradar.playlist.domain.port.in.*;
import edu.dosw.styleradar.playlist.infrastructure.adapter.in.rest.doc.PlaylistApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistController implements PlaylistApi {

    private final CreatePlaylistUseCase createPlaylistUseCase;
    private final GetPlaylistUseCase getPlaylistUseCase;
    private final FeaturePlaylistUseCase featurePlaylistUseCase;
    private final ManagePlaylistItemUseCase managePlaylistItemUseCase;
    private final InteractWithPlaylistUseCase interactWithPlaylistUseCase;

    public PlaylistController(
            CreatePlaylistUseCase createPlaylistUseCase,
            GetPlaylistUseCase getPlaylistUseCase,
            FeaturePlaylistUseCase featurePlaylistUseCase,
            ManagePlaylistItemUseCase managePlaylistItemUseCase,
            InteractWithPlaylistUseCase interactWithPlaylistUseCase) {

        this.createPlaylistUseCase = createPlaylistUseCase;
        this.getPlaylistUseCase = getPlaylistUseCase;
        this.featurePlaylistUseCase = featurePlaylistUseCase;
        this.managePlaylistItemUseCase = managePlaylistItemUseCase;
        this.interactWithPlaylistUseCase = interactWithPlaylistUseCase;
    }

    @Override
    @PostMapping
    public ResponseEntity<PlaylistResponse> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreatePlaylistRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        createPlaylistUseCase.create(
                                validateUserId(userId),
                                request
                        )
                );
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getById(
            @PathVariable UUID id,
            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            ) String userId) {

        String normalizedUserId =
                userId == null || userId.isBlank()
                        ? null
                        : userId.trim();

        return ResponseEntity.ok(
                getPlaylistUseCase.getById(
                        id,
                        normalizedUserId
                )
        );
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> getPublicPlaylists() {
        return ResponseEntity.ok(
                getPlaylistUseCase.getPublicPlaylists()
        );
    }

    @Override
    @GetMapping("/featured")
    public ResponseEntity<List<PlaylistResponse>> getFeaturedPlaylists() {
        return ResponseEntity.ok(
                getPlaylistUseCase.getFeaturedPlaylists()
        );
    }

    @Override
    @GetMapping("/popular")
    public ResponseEntity<PageResponse<PlaylistResponse>> getPopularPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                getPlaylistUseCase.getPopularPlaylists(page, size)
        );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> update(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlaylistRequest request) {

        return ResponseEntity.ok(
                createPlaylistUseCase.update(
                        validateUserId(userId),
                        id,
                        request
                )
        );
    }

    @Override
    @PutMapping("/{id}/feature")
    public ResponseEntity<PlaylistResponse> feature(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                featurePlaylistUseCase.feature(
                        validateUserId(userId),
                        id
                )
        );
    }

    @Override
    @DeleteMapping("/{id}/feature")
    public ResponseEntity<PlaylistResponse> removeFeature(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                featurePlaylistUseCase.removeFeature(
                        validateUserId(userId),
                        id
                )
        );
    }

    @Override
    @PostMapping("/{id}/items")
    public ResponseEntity<PlaylistResponse> addItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @Valid @RequestBody AddItemRequest request) {

        return ResponseEntity.ok(
                managePlaylistItemUseCase.addItem(
                        validateUserId(userId),
                        id,
                        request
                )
        );
    }

    @Override
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<PlaylistResponse> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @PathVariable UUID itemId) {

        return ResponseEntity.ok(
                managePlaylistItemUseCase.removeItem(
                        validateUserId(userId),
                        id,
                        itemId
                )
        );
    }

    @Override
    @PostMapping("/{id}/likes")
    public ResponseEntity<PlaylistResponse> toggleLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                interactWithPlaylistUseCase.toggleLike(
                        validateUserId(userId),
                        id
                )
        );
    }

    @Override
    @PostMapping("/{id}/saved")
    public ResponseEntity<PlaylistResponse> toggleSaved(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                interactWithPlaylistUseCase.toggleSaved(
                        validateUserId(userId),
                        id
                )
        );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {

        createPlaylistUseCase.delete(
                validateUserId(userId),
                id
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                getPlaylistUseCase.getByUser(
                        validateUserId(userId)
                )
        );
    }

    @Override
    @GetMapping("/me/saved")
    public ResponseEntity<List<PlaylistResponse>> getMySavedPlaylists(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(
                getPlaylistUseCase.getSavedPlaylists(
                        validateUserId(userId)
                )
        );
    }

    private String validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException(
                    "La cabecera X-User-Id es obligatoria y no puede estar vacía"
            );
        }

        return userId.trim();
    }
}