package edu.dosw.styleradar.playlist.domain.port.in;

import edu.dosw.styleradar.playlist.application.dto.PageResponse;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarPlaylistUseCase {
    PlaylistResponse obtenerPorId(UUID id, String usuarioConsultaId);
    List<PlaylistResponse> obtenerPublicas(String usuarioConsultaId);
    PageResponse<PlaylistResponse> obtenerPopulares(int page, int size, String usuarioConsultaId);
    List<PlaylistResponse> obtenerPorUsuario(String usuarioId);
    List<PlaylistResponse> obtenerGuardadas(String usuarioId);
    List<PlaylistResponse> obtenerDestacadas(String usuarioConsultaId);
}
