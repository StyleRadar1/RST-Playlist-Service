package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PageResponse;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarPlaylistUseCase {
    PlaylistResponse obtenerPorId(UUID id, UUID usuarioConsultaId);
    List<PlaylistResponse> obtenerPublicas();
    PageResponse<PlaylistResponse> obtenerPopulares(int page, int size);
    List<PlaylistResponse> obtenerPorUsuario(UUID usuarioId);
    List<PlaylistResponse> obtenerGuardadas(UUID usuarioId);
    List<PlaylistResponse> obtenerDestacadas();
}
