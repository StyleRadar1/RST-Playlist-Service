package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.application.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PrendaEmbeddable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PostgreSQLPlaylistRepositoryAdapter implements PlaylistRepositoryPort {

    private final SpringDataPlaylistRepository springDataRepository;

    public PostgreSQLPlaylistRepositoryAdapter(SpringDataPlaylistRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Playlist save(Playlist domain) {
        PlaylistEntity entity = toEntity(domain);
        PlaylistEntity guardada = springDataRepository.save(entity);
        return toDomain(guardada);
    }

    @Override
    public Optional<Playlist> findById(Long id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Playlist> findAllPublicas() {
        return springDataRepository.findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist.PUBLICA)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Playlist> findPopulares(int page, int size) {
        return springDataRepository.findPopulares(PageRequest.of(page, size))
                .getContent().stream().map(this::toDomain).toList();
    }

    @Override
    public long countPublicas() {
        return springDataRepository.countByVisibilidad(VisibilidadPlaylist.PUBLICA);
    }

    @Override
    public List<Playlist> findByUsuarioId(Long usuarioId) {
        return springDataRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Playlist> findByGuardadosUsuarioId(Long usuarioId) {
        return springDataRepository.findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(usuarioId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Playlist> findDestacadas() {
        return springDataRepository.findByDestacadaTrueOrderByFechaActualizacionDesc()
                .stream().limit(3).map(this::toDomain).toList();
    }

    @Override
    public long countDestacadas() {
        return springDataRepository.countByDestacadaTrue();
    }

    @Override
    public void delete(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<Playlist> findAllConPrenda(Long prendaId) {
        return springDataRepository.findAllByPrendaId(prendaId)
                .stream().map(this::toDomain).toList();
    }

    public Playlist toDomain(PlaylistEntity entity) {
        List<PrendaSnapshot> prendas = entity.getPrendas().stream()
                .map(p -> new PrendaSnapshot(p.getPrendaId(), p.getNombrePrenda(), p.getFechaAsociacion()))
                .toList();
        return new Playlist(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getVisibilidad(),
                entity.getEstilo(),
                entity.isDestacada(),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion(),
                prendas,
                entity.getLikesUsuarios(),
                entity.getGuardadosUsuarios()
        );
    }

    public PlaylistEntity toEntity(Playlist domain) {
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(domain.getId());
        entity.setUsuarioId(domain.getUsuarioId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setVisibilidad(domain.getVisibilidad());
        entity.setEstilo(domain.getEstilo());
        entity.setDestacada(domain.isDestacada());
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaActualizacion(domain.getFechaActualizacion());
        List<PrendaEmbeddable> prendas = domain.getPrendas().stream()
                .map(p -> new PrendaEmbeddable(p.getPrendaId(), p.getNombrePrenda(), p.getFechaAsociacion()))
                .toList();
        entity.setPrendas(new ArrayList<>(prendas));
        entity.setLikesUsuarios(new java.util.HashSet<>(domain.getLikesUsuarios()));
        entity.setGuardadosUsuarios(new java.util.HashSet<>(domain.getGuardadosUsuarios()));
        return entity;
    }
}
