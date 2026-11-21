package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.CrearPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.EditarPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.CodigoError;
import edu.dosw.styleradar.playlist.domain.exception.DomainException;
import edu.dosw.styleradar.playlist.domain.exception.LimitePrendasAlcanzadoException;
import edu.dosw.styleradar.playlist.domain.exception.PlaylistSinPrendasException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoAprobadaException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.port.in.CrearPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.port.out.PrendaCatalogoInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CrearPlaylistUseCaseImpl implements CrearPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;
    private final ItemCatalogoPort itemCatalogoPort;
    private final PlaylistEventPublisherPort eventPublisher;

    public CrearPlaylistUseCaseImpl(PlaylistRepositoryPort repository,
                                    PlaylistDtoMapper mapper,
                                    ItemCatalogoPort itemCatalogoPort,
                                    PlaylistEventPublisherPort eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemCatalogoPort = itemCatalogoPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
<<<<<<< Updated upstream
    public PlaylistResponse crear(UUID usuarioId, CrearPlaylistRequest request) {
        Playlist playlist = Playlist.crear(
=======
    public PlaylistResponse crear(String usuarioId, CrearPlaylistRequest request) {
        Playlist.crear(usuarioId, request.nombre(), request.descripcion(), request.visibilidad(), request.estilo());

        List<UUID> prendasIds = request.prendasIds();
        if (prendasIds == null || prendasIds.isEmpty()) {
            throw new PlaylistSinPrendasException("La playlist debe crearse con al menos una prenda (SR-RF-69)");
        }
        if (prendasIds.size() > Playlist.MAX_PRENDAS) {
            throw new LimitePrendasAlcanzadoException("Límite máximo de " + Playlist.MAX_PRENDAS + " prendas alcanzado");
        }

        List<PrendaSnapshot> snapshots = new ArrayList<>();
        for (UUID prendaId : prendasIds) {
            if (prendaId == null) {
                throw new DomainException(CodigoError.DATOS_INVALIDOS, "El ID de prenda no puede ser nulo");
            }
            PrendaCatalogoInfo prenda = itemCatalogoPort.obtenerPrendaPorId(prendaId)
                    .orElseThrow(() -> new PrendaNoEncontradaEnCatalogoException(
                            "La prenda " + prendaId + " no existe en ms-item-catalogo"));
            if (!prenda.aprobada()) {
                throw new PrendaNoAprobadaException(
                        "La prenda " + prendaId + " no se encuentra en estado APROBADA");
            }
            snapshots.add(new PrendaSnapshot(prendaId, prenda.nombre(), Instant.now(), true));
        }

        Playlist playlist = Playlist.crearConPrendas(
>>>>>>> Stashed changes
                usuarioId,
                request.nombre(),
                request.descripcion(),
                request.visibilidad(),
                request.estilo(),
                snapshots
        );
        Playlist guardada = repository.save(playlist);
        guardada.getPrendas().forEach(prenda ->
                eventPublisher.publicarPrendaAsociada(guardada.getId(), prenda.getPrendaId()));
        return mapper.toResponse(guardada);
    }

    @Override
    public PlaylistResponse editar(UUID usuarioId, UUID playlistId, EditarPlaylistRequest request) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.editar(usuarioId, request.nombre(), request.descripcion(), request.visibilidad(), request.estilo());
        return mapper.toResponse(repository.save(playlist));
    }

    @Override
    public void eliminar(UUID usuarioId, UUID playlistId) {
        Playlist playlist = repository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + playlistId));
        playlist.validarPropietario(usuarioId);
        repository.delete(playlistId);
    }
}
