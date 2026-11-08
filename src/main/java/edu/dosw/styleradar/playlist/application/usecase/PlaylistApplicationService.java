package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.port.in.PlaylistUseCasePort;
import edu.dosw.styleradar.playlist.application.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.application.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PlaylistApplicationService implements PlaylistUseCasePort {

    private final PlaylistRepositoryPort repository;
    private final PlaylistEventPublisherPort eventPublisher;

    public PlaylistApplicationService(PlaylistRepositoryPort repository, PlaylistEventPublisherPort eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PlaylistResponse crear(Long usuarioId, CrearPlaylistRequest request) {
        Playlist playlist = Playlist.crear(
                usuarioId,
                request.nombre(),
                request.descripcion(),
                request.visibilidad(),
                request.estilo()
        );
        Playlist guardada = repository.save(playlist);
        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistResponse obtenerPorId(Long id, Long usuarioConsultaId) {
        Playlist playlist = buscarPorId(id);
        playlist.validarAccesoLectura(usuarioConsultaId);
        return toResponse(playlist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> obtenerPublicas() {
        return repository.findAllPublicas().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlaylistResponse> obtenerPopulares(int page, int size) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = (size < 1 || size > 100) ? 20 : size;
        List<PlaylistResponse> content = repository.findPopulares(sanitizedPage, sanitizedSize)
                .stream().map(this::toResponse).toList();
        long total = repository.countPublicas();
        int totalPages = (int) Math.ceil((double) total / sanitizedSize);
        return new PageResponse<>(
                content,
                sanitizedPage,
                sanitizedSize,
                total,
                totalPages,
                sanitizedPage == 0,
                sanitizedPage >= totalPages - 1
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> obtenerPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> obtenerGuardadas(Long usuarioId) {
        return repository.findByGuardadosUsuarioId(usuarioId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> obtenerDestacadas() {
        return repository.findDestacadas().stream().map(this::toResponse).toList();
    }

    @Override
    public PlaylistResponse editar(Long usuarioId, Long playlistId, EditarPlaylistRequest request) {
        Playlist playlist = buscarPorId(playlistId);
        playlist.editar(usuarioId, request.nombre(), request.descripcion(), request.visibilidad(), request.estilo());
        return toResponse(repository.save(playlist));
    }

    @Override
    public PlaylistResponse destacar(Long usuarioId, Long playlistId) {
        Playlist playlist = buscarPorId(playlistId);
        if (!playlist.isDestacada()) {
            long totalDestacadas = repository.countDestacadas();
            if (totalDestacadas >= Playlist.MAX_DESTACADAS) {
                throw new LimiteDestacadasSuperadoException("Límite máximo de " + Playlist.MAX_DESTACADAS + " playlists destacadas alcanzado (SR-RF-72)");
            }
            playlist.destacar();
            Playlist guardada = repository.save(playlist);
            eventPublisher.publicarPlaylistDestacadaActualizada(guardada.getId(), true);
            return toResponse(guardada);
        }
        return toResponse(playlist);
    }

    @Override
    public PlaylistResponse quitarDestacado(Long usuarioId, Long playlistId) {
        Playlist playlist = buscarPorId(playlistId);
        if (playlist.isDestacada()) {
            playlist.quitarDestacado();
            Playlist guardada = repository.save(playlist);
            eventPublisher.publicarPlaylistDestacadaActualizada(guardada.getId(), false);
            return toResponse(guardada);
        }
        return toResponse(playlist);
    }

    @Override
    public PlaylistResponse agregarPrenda(Long usuarioId, Long playlistId, AgregarPrendaRequest request) {
        Playlist playlist = buscarPorId(playlistId);
        playlist.validarPropietario(usuarioId);
        PrendaSnapshot snapshot = new PrendaSnapshot(request.prendaId(), request.nombrePrenda(), Instant.now());
        playlist.agregarPrenda(snapshot);
        Playlist guardada = repository.save(playlist);
        eventPublisher.publicarPrendaAsociada(guardada.getId(), request.prendaId());
        return toResponse(guardada);
    }

    @Override
    public PlaylistResponse retirarPrenda(Long usuarioId, Long playlistId, Long prendaId) {
        Playlist playlist = buscarPorId(playlistId);
        playlist.retirarPrenda(prendaId, usuarioId);
        return toResponse(repository.save(playlist));
    }

    @Override
    public PlaylistResponse alternarLike(Long usuarioId, Long playlistId) {
        Playlist playlist = buscarPorId(playlistId);
        playlist.alternarLike(usuarioId);
        return toResponse(repository.save(playlist));
    }

    @Override
    public PlaylistResponse alternarGuardado(Long usuarioId, Long playlistId) {
        Playlist playlist = buscarPorId(playlistId);
        playlist.alternarGuardado(usuarioId);
        return toResponse(repository.save(playlist));
    }

    @Override
    public void eliminar(Long usuarioId, Long playlistId) {
        Playlist playlist = buscarPorId(playlistId);
        playlist.validarPropietario(usuarioId);
        repository.delete(playlistId);
    }

    @Override
    public void desvincularPrendaDeTodas(Long prendaId) {
        List<Playlist> afectadas = repository.findAllConPrenda(prendaId);
        for (Playlist p : afectadas) {
            if (p.desvincularPrendaSilencioso(prendaId)) {
                repository.save(p);
            }
        }
    }

    private Playlist buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una playlist con ID: " + id));
    }

    private PlaylistResponse toResponse(Playlist p) {
        List<PrendaResponse> prendas = p.getPrendas().stream()
                .map(pr -> new PrendaResponse(pr.getPrendaId(), pr.getNombrePrenda(), pr.getFechaAsociacion()))
                .toList();
        return new PlaylistResponse(
                p.getId(),
                p.getUsuarioId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getVisibilidad(),
                p.getEstilo(),
                p.isDestacada(),
                p.getFechaCreacion(),
                prendas,
                p.getLikesCount(),
                p.getGuardadosCount(),
                p.getTotalPrendas()
        );
    }
}
