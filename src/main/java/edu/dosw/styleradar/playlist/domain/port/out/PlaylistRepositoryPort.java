package edu.dosw.styleradar.playlist.domain.port.out;

import edu.dosw.styleradar.playlist.domain.model.Playlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepositoryPort {
    Playlist save(Playlist playlist);
    Optional<Playlist> findById(UUID id);
    List<Playlist> findAllPublicas();
    List<Playlist> findPopulares(int page, int size);
    long countPublicas();
    List<Playlist> findByUsuarioId(UUID usuarioId);
    List<Playlist> findByGuardadosUsuarioId(UUID usuarioId);
    List<Playlist> findDestacadas();
    long countDestacadas();
    void delete(UUID id);
    List<Playlist> findAllConPrenda(UUID prendaId);
}
