package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.PageResponse;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.ConsultarPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarPlaylistUseCaseImpl implements ConsultarPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;

    public ConsultarPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse obtenerPorId(UUID id, UUID usuarioConsultaId) {
        Playlist playlist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe playlist con ID: " + id));
        playlist.validarAccesoLectura(usuarioConsultaId);
        return mapper.toResponse(playlist);
    }

    @Override
    public List<PlaylistResponse> obtenerPublicas() {
        return repository.findAllPublicas().stream().map(mapper::toResponse).toList();
    }

    @Override
    public PageResponse<PlaylistResponse> obtenerPopulares(int page, int size) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = (size < 1 || size > 100) ? 20 : size;
        List<PlaylistResponse> content = repository.findPopulares(sanitizedPage, sanitizedSize)
                .stream().map(mapper::toResponse).toList();
        long total = repository.countPublicas();
        int totalPages = (int) Math.ceil((double) total / sanitizedSize);
        return new PageResponse<>(content, sanitizedPage, sanitizedSize, total, totalPages, sanitizedPage == 0, sanitizedPage >= totalPages - 1);
    }

    @Override
    public List<PlaylistResponse> obtenerPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<PlaylistResponse> obtenerGuardadas(UUID usuarioId) {
        return repository.findByGuardadosUsuarioId(usuarioId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<PlaylistResponse> obtenerDestacadas() {
        return repository.findDestacadas().stream().map(mapper::toResponse).toList();
    }
}
