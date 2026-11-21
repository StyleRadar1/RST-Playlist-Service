package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface InteractuarPlaylistUseCase {
    PlaylistResponse alternarLike(String usuarioId, UUID playlistId);
    PlaylistResponse alternarGuardado(String usuarioId, UUID playlistId);
}
