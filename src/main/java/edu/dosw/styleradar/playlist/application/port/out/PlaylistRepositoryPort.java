package edu.dosw.styleradar.playlist.application.port.out;

import edu.dosw.styleradar.playlist.domain.model.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepositoryPort {
    Playlist save(Playlist playlist);
    Optional<Playlist> findById(Long id);
    List<Playlist> findAllPublicas();
    List<Playlist> findPopulares(int page, int size);
    long countPublicas();
    List<Playlist> findByUsuarioId(Long usuarioId);
    List<Playlist> findByGuardadosUsuarioId(Long usuarioId);
    List<Playlist> findDestacadas();
    long countDestacadas();
    void delete(Long id);
    List<Playlist> findAllConPrenda(Long prendaId);
}
