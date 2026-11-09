package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.InteractuarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class InteractuarPlaylistUseCaseImpl implements InteractuarPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;

    public InteractuarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse alternarLike(UUID usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.alternarLike(usuarioId);
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public PlaylistResponse alternarGuardado(UUID usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.alternarGuardado(usuarioId);
        return mapper.toResponse(repository.save(playlist));
    }
}
