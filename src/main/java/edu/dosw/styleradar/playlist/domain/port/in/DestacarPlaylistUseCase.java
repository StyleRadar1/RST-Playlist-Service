package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface DestacarPlaylistUseCase {
    PlaylistResponse destacar(UUID usuarioId, UUID playlistId);
    PlaylistResponse quitarDestacado(UUID usuarioId, UUID playlistId);
}
