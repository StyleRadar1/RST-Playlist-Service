package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.ResourceNotFoundException;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCasesTest {

    @Mock
    private PlaylistRepositoryPort repository;

    @Mock
    private PlaylistEventPublisherPort eventPublisher;

    private final PlaylistDtoMapper mapper = Mappers.getMapper(PlaylistDtoMapper.class);

    private CrearPlaylistUseCaseImpl crearUseCase;
    private ConsultarPlaylistUseCaseImpl consultarUseCase;
    private DestacarPlaylistUseCaseImpl destacarUseCase;
    private AsociarPrendaUseCaseImpl asociarPrendaUseCase;
    private InteractuarPlaylistUseCaseImpl interactuarUseCase;

    @BeforeEach
    void setUp() {
        crearUseCase = new CrearPlaylistUseCaseImpl(repository, mapper);
        consultarUseCase = new ConsultarPlaylistUseCaseImpl(repository, mapper);
        destacarUseCase = new DestacarPlaylistUseCaseImpl(repository, eventPublisher, mapper);
        asociarPrendaUseCase = new AsociarPrendaUseCaseImpl(repository, eventPublisher, mapper);
        interactuarUseCase = new InteractuarPlaylistUseCaseImpl(repository, mapper);
    }

    @Test
    void crearYEditarPlaylist() {
        UUID usuarioId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.setId(playlistId);

        when(repository.save(any(Playlist.class))).thenReturn(p);
        when(repository.findById(playlistId)).thenReturn(Optional.of(p));

        PlaylistResponse resCrear = crearUseCase.crear(usuarioId, new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO));
        assertThat(resCrear.id()).isEqualTo(playlistId);

        PlaylistResponse resEditar = crearUseCase.editar(usuarioId, playlistId, new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL));
        assertThat(resEditar.nombre()).isEqualTo("Nuevo");

        crearUseCase.eliminar(usuarioId, playlistId);
        verify(repository).delete(playlistId);
    }

    @Test
    void consultasPlaylist() {
        UUID usuarioId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.findAllPublicas()).thenReturn(List.of(p));
        when(repository.findPopulares(0, 10)).thenReturn(List.of(p));
        when(repository.countPublicas()).thenReturn(1L);
        when(repository.findByUsuarioId(usuarioId)).thenReturn(List.of(p));
        when(repository.findByGuardadosUsuarioId(usuarioId)).thenReturn(List.of(p));
        when(repository.findDestacadas()).thenReturn(List.of(p));

        assertThat(consultarUseCase.obtenerPorId(playlistId, usuarioId).id()).isEqualTo(playlistId);
        assertThat(consultarUseCase.obtenerPublicas()).hasSize(1);
        assertThat(consultarUseCase.obtenerPopulares(0, 10).content()).hasSize(1);
        assertThat(consultarUseCase.obtenerPorUsuario(usuarioId)).hasSize(1);
        assertThat(consultarUseCase.obtenerGuardadas(usuarioId)).hasSize(1);
        assertThat(consultarUseCase.obtenerDestacadas()).hasSize(1);
    }

    @Test
    void destacarPlaylistValidaTope() {
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(UUID.randomUUID(), "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.countDestacadas()).thenReturn(3L);

        assertThatThrownBy(() -> destacarUseCase.destacar(UUID.randomUUID(), playlistId))
                .isInstanceOf(LimiteDestacadasSuperadoException.class);
    }

    @Test
    void asociarYRetirarPrenda() {
        UUID usuarioId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = asociarPrendaUseCase.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaId, "Gorra"));
        assertThat(res.totalPrendas()).isEqualTo(1);

        PlaylistResponse resRetiro = asociarPrendaUseCase.retirarPrenda(usuarioId, playlistId, prendaId);
        assertThat(resRetiro.totalPrendas()).isZero();

        when(repository.findAllConPrenda(prendaId)).thenReturn(List.of(p));
        asociarPrendaUseCase.desvincularPrendaDeTodas(prendaId);
    }

    @Test
    void interactuarLikeYGuardado() {
        UUID usuarioId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.save(any())).thenReturn(p);

        interactuarUseCase.alternarLike(usuarioId, playlistId);
        interactuarUseCase.alternarGuardado(usuarioId, playlistId);

        verify(repository, times(2)).save(p);
    }
}
