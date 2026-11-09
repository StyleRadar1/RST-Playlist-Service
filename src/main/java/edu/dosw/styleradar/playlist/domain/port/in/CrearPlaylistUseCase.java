package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.CrearPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.EditarPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface CrearPlaylistUseCase {
    PlaylistResponse crear(UUID usuarioId, CrearPlaylistRequest request);
    PlaylistResponse editar(UUID usuarioId, UUID playlistId, EditarPlaylistRequest request);
    void eliminar(UUID usuarioId, UUID playlistId);
}
