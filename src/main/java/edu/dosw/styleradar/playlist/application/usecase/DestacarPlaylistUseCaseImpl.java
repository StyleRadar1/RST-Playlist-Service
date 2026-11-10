package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.DestacarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DestacarPlaylistUseCaseImpl implements DestacarPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistEventPublisherPort eventPublisher;
    private final PlaylistDtoMapper mapper;

    public DestacarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistEventPublisherPort eventPublisher, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse destacar(UUID usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        if (!playlist.isDestacada()) {
            if (repository.countDestacadas() >= Playlist.MAX_DESTACADAS) {
                throw new LimiteDestacadasSuperadoException("Límite máximo de " + Playlist.MAX_DESTACADAS + " playlists destacadas alcanzado (SR-RF-72)");
            }
            playlist.destacar();
            Playlist guardada = repository.save(playlist);
            eventPublisher.publicarPlaylistDestacadaActualizada(guardada.getId(), true);
            return mapper.toResponse(guardada);
        }
        return mapper.toResponse(playlist);
    }

    @Override
    public PlaylistResponse quitarDestacado(UUID usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        if (playlist.isDestacada()) {
            playlist.quitarDestacado();
            Playlist guardada = repository.save(playlist);
            eventPublisher.publicarPlaylistDestacadaActualizada(guardada.getId(), false);
            return mapper.toResponse(guardada);
        }
        return mapper.toResponse(playlist);
    }
}
