package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.CrearPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.EditarPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.UUID;

public interface CrearPlaylistUseCase {
    PlaylistResponse crear(String usuarioId, CrearPlaylistRequest request);
    PlaylistResponse editar(String usuarioId, UUID playlistId, EditarPlaylistRequest request);
    void eliminar(String usuarioId, UUID playlistId);
}
