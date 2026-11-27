package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PageResponse;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.port.in.ConsultarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.port.out.PrendaCatalogoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarPlaylistUseCaseImpl implements ConsultarPlaylistUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPlaylistUseCaseImpl.class);

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;
    private final ItemCatalogoPort itemCatalogoPort;

    public ConsultarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this(repository, mapper, null);
    }

    @Autowired
    public ConsultarPlaylistUseCaseImpl(PlaylistRepositoryPort repository,
                                        PlaylistDtoMapper mapper,
                                        ItemCatalogoPort itemCatalogoPort) {
        this.repository = repository;
        this.mapper = mapper;
        this.itemCatalogoPort = itemCatalogoPort;
    }

    @Override
    public PlaylistResponse obtenerPorId(UUID id, String usuarioConsultaId) {
        Playlist playlist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + id));
        playlist.validarAccesoLectura(usuarioConsultaId);
        enriquecerPrendas(List.of(playlist));
        return mapper.toResponse(playlist, usuarioConsultaId);
    }

    @Override
    public List<PlaylistResponse> obtenerPublicas(String usuarioConsultaId) {
        List<Playlist> playlists = repository.findAllPublicas();
        enriquecerPrendas(playlists);
        return playlists.stream().map(p -> mapper.toResponse(p, usuarioConsultaId)).toList();
    }

    @Override
    public PageResponse<PlaylistResponse> obtenerPopulares(int page, int size, String usuarioConsultaId) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = (size < 1 || size > 100) ? 20 : size;
        List<Playlist> playlists = repository.findPopulares(sanitizedPage, sanitizedSize);
        enriquecerPrendas(playlists);
        List<PlaylistResponse> content = playlists.stream().map(p -> mapper.toResponse(p, usuarioConsultaId)).toList();
        long total = repository.countPublicas();
        int totalPages = (int) Math.ceil((double) total / sanitizedSize);
        return new PageResponse<>(content, sanitizedPage, sanitizedSize, total, totalPages, sanitizedPage == 0, sanitizedPage >= totalPages - 1);
    }

    @Override
    public List<PlaylistResponse> obtenerPorUsuario(String usuarioId) {
        List<Playlist> playlists = repository.findByUsuarioId(usuarioId);
        enriquecerPrendas(playlists);
        return playlists.stream().map(p -> mapper.toResponse(p, usuarioId)).toList();
    }

    @Override
    public List<PlaylistResponse> obtenerGuardadas(String usuarioId) {
        List<Playlist> playlists = repository.findByGuardadosUsuarioId(usuarioId);
        enriquecerPrendas(playlists);
        return playlists.stream().map(p -> mapper.toResponse(p, usuarioId)).toList();
    }

    @Override
    public List<PlaylistResponse> obtenerDestacadas(String usuarioConsultaId) {
        List<Playlist> playlists = repository.findDestacadas();
        enriquecerPrendas(playlists);
        return playlists.stream().map(p -> mapper.toResponse(p, usuarioConsultaId)).toList();
    }

    private void enriquecerPrendas(List<Playlist> playlists) {
        if (itemCatalogoPort == null || playlists == null || playlists.isEmpty()) {
            return;
        }
        Map<UUID, Optional<PrendaCatalogoInfo>> cache = new HashMap<>();
        for (Playlist playlist : playlists) {
            for (PrendaSnapshot prenda : playlist.getPrendas()) {
                if (!prenda.necesitaEnriquecimiento()) {
                    continue;
                }
                Optional<PrendaCatalogoInfo> info = cache.computeIfAbsent(prenda.getPrendaId(), this::consultarCatalogoSeguro);
                info.ifPresent(catalogo -> prenda.completarDatosCatalogo(catalogo.imagenUrl(), catalogo.precio()));
            }
        }
    }

    private Optional<PrendaCatalogoInfo> consultarCatalogoSeguro(UUID prendaId) {
        try {
            return itemCatalogoPort.obtenerPrendaPorId(prendaId);
        } catch (RuntimeException ex) {
            log.warn("No se pudo enriquecer imagen/precio de la prenda {} desde ms-item-catalogo: {}",
                    prendaId, ex.getMessage());
            return Optional.empty();
        }
    }
}
