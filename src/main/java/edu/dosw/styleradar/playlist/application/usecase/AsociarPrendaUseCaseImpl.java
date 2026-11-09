package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.AgregarPrendaRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AsociarPrendaUseCaseImpl implements AsociarPrendaUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistEventPublisherPort eventPublisher;
    private final PlaylistDtoMapper mapper;

    public AsociarPrendaUseCaseImpl(PlaylistRepositoryPort repository, PlaylistEventPublisherPort eventPublisher, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse agregarPrenda(UUID usuarioId, UUID playlistId, AgregarPrendaRequest request) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.validarPropietario(usuarioId);
        PrendaSnapshot snapshot = new PrendaSnapshot(request.prendaId(), request.nombrePrenda(), Instant.now());
        playlist.agregarPrenda(snapshot);
        Playlist guardada = repository.save(playlist);
        eventPublisher.publicarPrendaAsociada(guardada.getId(), request.prendaId());
        return mapper.toResponse(guardada);
    }

    @Override
    public PlaylistResponse retirarPrenda(UUID usuarioId, UUID playlistId, UUID prendaId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.retirarPrenda(prendaId, usuarioId);
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public void desvincularPrendaDeTodas(UUID prendaId) {
        List<Playlist> afectadas = repository.findAllConPrenda(prendaId);
        for (Playlist p : afectadas) {
            if (p.desvincularPrendaSilencioso(prendaId)) {
                repository.save(p);
            }
        }
    }
}
