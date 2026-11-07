package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLPlaylistRepositoryAdapterTest {

    @Mock
    private SpringDataPlaylistRepository springDataRepository;

    private PostgreSQLPlaylistRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PostgreSQLPlaylistRepositoryAdapter(springDataRepository);
    }

    @Test
    void guardarYRecuperarPlaylist() {
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(1L);
        entity.setUsuarioId(10L);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.save(any())).thenReturn(entity);
        when(springDataRepository.findById(1L)).thenReturn(Optional.of(entity));

        Playlist p = Playlist.crear(10L, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        Playlist guardada = adapter.save(p);
        Optional<Playlist> opt = adapter.findById(1L);

        assertThat(guardada.getId()).isEqualTo(1L);
        assertThat(opt).isPresent();
    }

    @Test
    void consultasMultiples() {
        PlaylistEntity entity = new PlaylistEntity();
        entity.setId(1L);
        entity.setUsuarioId(10L);
        entity.setNombre("Test");
        entity.setVisibilidad(VisibilidadPlaylist.PUBLICA);
        entity.setEstilo(Estilo.URBANO);
        entity.setFechaCreacion(Instant.now());
        entity.setFechaActualizacion(Instant.now());

        when(springDataRepository.findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist.PUBLICA)).thenReturn(List.of(entity));
        when(springDataRepository.findPopulares(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(springDataRepository.countByVisibilidad(VisibilidadPlaylist.PUBLICA)).thenReturn(1L);
        when(springDataRepository.findByUsuarioIdOrderByFechaCreacionDesc(10L)).thenReturn(List.of(entity));
        when(springDataRepository.findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(10L)).thenReturn(List.of(entity));
        when(springDataRepository.findByDestacadaTrueOrderByFechaActualizacionDesc()).thenReturn(List.of(entity));
        when(springDataRepository.countByDestacadaTrue()).thenReturn(1L);
        when(springDataRepository.findAllByPrendaId(5L)).thenReturn(List.of(entity));

        assertThat(adapter.findAllPublicas()).hasSize(1);
        assertThat(adapter.findPopulares(0, 10)).hasSize(1);
        assertThat(adapter.countPublicas()).isEqualTo(1L);
        assertThat(adapter.findByUsuarioId(10L)).hasSize(1);
        assertThat(adapter.findByGuardadosUsuarioId(10L)).hasSize(1);
        assertThat(adapter.findDestacadas()).hasSize(1);
        assertThat(adapter.countDestacadas()).isEqualTo(1L);
        assertThat(adapter.findAllConPrenda(5L)).hasSize(1);

        adapter.delete(1L);
        verify(springDataRepository).deleteById(1L);
    }
}
