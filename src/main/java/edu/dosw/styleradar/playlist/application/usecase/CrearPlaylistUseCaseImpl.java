package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.CrearPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.EditarPlaylistRequest;
import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.port.in.CrearPlaylistUseCase;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CrearPlaylistUseCaseImpl implements CrearPlaylistUseCase {

    private final PlaylistRepositoryPort repository;
    private final PlaylistDtoMapper mapper;

    public CrearPlaylistUseCaseImpl(PlaylistRepositoryPort repository, PlaylistDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PlaylistResponse crear(UUID usuarioId, CrearPlaylistRequest request) {
        Playlist playlist = Playlist.crear(
                usuarioId,
                request.nombre(),
                request.descripcion(),
                request.visibilidad(),
                request.estilo()
        );
        return mapper.toResponse(repository.save(playlist));
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
