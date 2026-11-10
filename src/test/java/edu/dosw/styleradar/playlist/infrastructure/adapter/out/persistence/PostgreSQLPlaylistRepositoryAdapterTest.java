package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.mapper.PlaylistEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLPlaylistRepositoryAdapterTest {

    @Mock
    private SpringDataPlaylistRepository springDataRepository;

    private final PlaylistEntityMapper mapper = Mappers.getMapper(PlaylistEntityMapper.class);

    private PostgreSQLPlaylistRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PostgreSQLPlaylistRepositoryAdapter(springDataRepository, mapper);
    }

    @Test
    void guardarYRecuperarPlaylist() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(id);
        entity.setUsuarioId(usuarioId);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.save(any())).thenReturn(entity);
        when(springDataRepository.findById(id)).thenReturn(Optional.of(entity));

        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(id);
        Playlist guardada = adapter.save(p);
        Optional<Playlist> opt = adapter.findById(id);

        assertThat(guardada.getId()).isEqualTo(id);
        assertThat(opt).isPresent();
    }

    @Test
    void consultasMultiples() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(id);
        entity.setUsuarioId(usuarioId);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist.PUBLICA)).thenReturn(List.of(entity));
        when(springDataRepository.findPopulares(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(springDataRepository.countByVisibilidad(VisibilidadPlaylist.PUBLICA)).thenReturn(1L);
        when(springDataRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)).thenReturn(List.of(entity));
        when(springDataRepository.findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(usuarioId)).thenReturn(List.of(entity));
        when(springDataRepository.findByDestacadaTrueOrderByFechaActualizacionDesc()).thenReturn(List.of(entity));
        when(springDataRepository.countByDestacadaTrue()).thenReturn(1L);
        when(springDataRepository.findAllByPrendaId(prendaId)).thenReturn(List.of(entity));

        assertThat(adapter.findAllPublicas()).hasSize(1);
        assertThat(adapter.findPopulares(0, 10)).hasSize(1);
        assertThat(adapter.countPublicas()).isEqualTo(1L);
        assertThat(adapter.findByUsuarioId(usuarioId)).hasSize(1);
        assertThat(adapter.findByGuardadosUsuarioId(usuarioId)).hasSize(1);
        assertThat(adapter.findDestacadas()).hasSize(1);
        assertThat(adapter.countDestacadas()).isEqualTo(1L);
        assertThat(adapter.findAllConPrenda(prendaId)).hasSize(1);

        adapter.delete(id);
        verify(springDataRepository).deleteById(id);
    }
}
