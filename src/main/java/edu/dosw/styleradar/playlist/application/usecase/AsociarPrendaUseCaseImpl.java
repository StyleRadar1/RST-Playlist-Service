package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.AgregarPrendaRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoAprobadaException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.port.out.PrendaCatalogoInfo;
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
    private final ItemCatalogoPort itemCatalogoPort;

    public AsociarPrendaUseCaseImpl(PlaylistRepositoryPort repository,
                                    PlaylistEventPublisherPort eventPublisher,
                                    PlaylistDtoMapper mapper,
                                    ItemCatalogoPort itemCatalogoPort) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.itemCatalogoPort = itemCatalogoPort;
    }

    @Override
    public PlaylistResponse agregarPrenda(String usuarioId, UUID playlistId, AgregarPrendaRequest request) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.validarPropietario(usuarioId);

        PrendaCatalogoInfo prenda = itemCatalogoPort.obtenerPrendaPorId(request.prendaId())
                .orElseThrow(() -> new PrendaNoEncontradaEnCatalogoException(
                        "La prenda " + request.prendaId() + " no existe en ms-item-catalogo"));
        if (!prenda.aprobada()) {
            throw new PrendaNoAprobadaException(
                    "La prenda " + request.prendaId() + " no se encuentra en estado APROBADA");
        }

        PrendaSnapshot snapshot = new PrendaSnapshot(
                request.prendaId(), prenda.nombre(), Instant.now(), true, prenda.imagenUrl(), prenda.precio());
        playlist.agregarPrenda(snapshot);
        Playlist guardada = repository.save(playlist);
        eventPublisher.publicarPrendaAsociada(guardada.getId(), request.prendaId());
        return mapper.toResponse(guardada);
    }

    @Override
    public PlaylistResponse retirarPrenda(String usuarioId, UUID playlistId, UUID prendaId) {
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

    @Override
    public void marcarPrendaNoDisponibleEnTodas(UUID prendaId) {
        List<Playlist> afectadas = repository.findAllConPrenda(prendaId);
        for (Playlist p : afectadas) {
            if (p.marcarPrendaNoDisponible(prendaId)) {
                repository.save(p);
            }
        }
    }
}
