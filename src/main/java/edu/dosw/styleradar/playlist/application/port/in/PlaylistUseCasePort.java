package edu.dosw.styleradar.playlist.application.port.in;

import edu.dosw.styleradar.playlist.application.dto.*;

import java.util.List;

public interface PlaylistUseCasePort {
    PlaylistResponse crear(Long usuarioId, CrearPlaylistRequest request);
    PlaylistResponse obtenerPorId(Long id, Long usuarioConsultaId);
    List<PlaylistResponse> obtenerPublicas();
    PageResponse<PlaylistResponse> obtenerPopulares(int page, int size);
    List<PlaylistResponse> obtenerPorUsuario(Long usuarioId);
    List<PlaylistResponse> obtenerGuardadas(Long usuarioId);
    List<PlaylistResponse> obtenerDestacadas();
    PlaylistResponse editar(Long usuarioId, Long playlistId, EditarPlaylistRequest request);
    PlaylistResponse destacar(Long usuarioId, Long playlistId);
    PlaylistResponse quitarDestacado(Long usuarioId, Long playlistId);
    PlaylistResponse agregarPrenda(Long usuarioId, Long playlistId, AgregarPrendaRequest request);
    PlaylistResponse retirarPrenda(Long usuarioId, Long playlistId, Long prendaId);
    PlaylistResponse alternarLike(Long usuarioId, Long playlistId);
    PlaylistResponse alternarGuardado(Long usuarioId, Long playlistId);
    void eliminar(Long usuarioId, Long playlistId);
    void desvincularPrendaDeTodas(Long prendaId);
}
