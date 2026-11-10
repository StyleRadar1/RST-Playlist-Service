package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.mapper.PlaylistEntityMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PostgreSQLPlaylistRepositoryAdapter implements PlaylistRepositoryPort {

    private final SpringDataPlaylistRepository springDataRepository;
    private final PlaylistEntityMapper mapper;

    public PostgreSQLPlaylistRepositoryAdapter(SpringDataPlaylistRepository springDataRepository, PlaylistEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Playlist save(Playlist domain) {
        PlaylistEntity entity = mapper.toEntity(domain);
        PlaylistEntity guardada = springDataRepository.save(entity);
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<Playlist> findById(UUID id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Playlist> findAllPublicas() {
        return springDataRepository.findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist.PUBLICA)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Playlist> findPopulares(int page, int size) {
        return springDataRepository.findPopulares(PageRequest.of(page, size))
                .getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countPublicas() {
        return springDataRepository.countByVisibilidad(VisibilidadPlaylist.PUBLICA);
    }

    @Override
    public List<Playlist> findByUsuarioId(UUID usuarioId) {
        return springDataRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Playlist> findByGuardadosUsuarioId(UUID usuarioId) {
        return springDataRepository.findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(usuarioId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Playlist> findDestacadas() {
        return springDataRepository.findByDestacadaTrueOrderByFechaActualizacionDesc()
                .stream().limit(3).map(mapper::toDomain).toList();
    }

    @Override
    public long countDestacadas() {
        return springDataRepository.countByDestacadaTrue();
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public List<Playlist> findAllConPrenda(UUID prendaId) {
        return springDataRepository.findAllByPrendaId(prendaId)
                .stream().map(mapper::toDomain).toList();
    }
}
