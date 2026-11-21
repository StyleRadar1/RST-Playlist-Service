package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface DestacarPlaylistUseCase {
    PlaylistResponse destacar(String usuarioId, UUID playlistId);
    PlaylistResponse quitarDestacado(String usuarioId, UUID playlistId);
}
