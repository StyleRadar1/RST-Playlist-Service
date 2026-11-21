package edu.dosw.styleradar.playlist.application.usecase;

import edu.dosw.styleradar.playlist.application.dto.*;
import edu.dosw.styleradar.playlist.application.dto.mapper.PlaylistDtoMapper;
import edu.dosw.styleradar.playlist.domain.exception.LimiteDestacadasSuperadoException;
import edu.dosw.styleradar.playlist.domain.exception.PlaylistSinPrendasException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoAprobadaException;
import edu.dosw.styleradar.playlist.domain.exception.PrendaNoEncontradaEnCatalogoException;
import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistEventPublisherPort;
import edu.dosw.styleradar.playlist.domain.port.out.PlaylistRepositoryPort;
import edu.dosw.styleradar.playlist.domain.port.out.PrendaCatalogoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private ItemCatalogoPort itemCatalogoPort;

    private final PlaylistDtoMapper mapper = Mappers.getMapper(PlaylistDtoMapper.class);

    private CrearPlaylistUseCaseImpl crearUseCase;
    private ConsultarPlaylistUseCaseImpl consultarUseCase;
    private DestacarPlaylistUseCaseImpl destacarUseCase;
    private AsociarPrendaUseCaseImpl asociarPrendaUseCase;
    private InteractuarPlaylistUseCaseImpl interactuarUseCase;

    @BeforeEach
    void setUp() {
        crearUseCase = new CrearPlaylistUseCaseImpl(repository, mapper, itemCatalogoPort, eventPublisher);
        consultarUseCase = new ConsultarPlaylistUseCaseImpl(repository, mapper);
        destacarUseCase = new DestacarPlaylistUseCaseImpl(repository, eventPublisher, mapper);
        asociarPrendaUseCase = new AsociarPrendaUseCaseImpl(repository, eventPublisher, mapper, itemCatalogoPort);
        interactuarUseCase = new InteractuarPlaylistUseCaseImpl(repository, mapper);
    }

    @Test
    void crearYEditarPlaylistConPrendaAprobada() {
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);

        when(itemCatalogoPort.obtenerPrendaPorId(prendaId))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaId, "Chaqueta Denim", true)));
        when(repository.save(any(Playlist.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findById(p.getId())).thenReturn(Optional.of(p));

        PlaylistResponse resCrear = crearUseCase.crear(usuarioId,
                new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, List.of(prendaId)));
        assertThat(resCrear.usuarioId().toString()).isEqualTo(usuarioId.toString());
        assertThat(resCrear.totalPrendas()).isEqualTo(1);
        assertThat(resCrear.prendas().getFirst().nombrePrenda()).isEqualTo("Chaqueta Denim");
        verify(eventPublisher).publicarPrendaAsociada(resCrear.id(), prendaId);

        PlaylistResponse resEditar = crearUseCase.editar(usuarioId, p.getId(),
                new EditarPlaylistRequest("Nuevo", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL));
        assertThat(resEditar.nombre()).isEqualTo("Nuevo");

        crearUseCase.eliminar(usuarioId, p.getId());
        verify(repository).delete(p.getId());
    }

    @Test
    void crearPlaylistValidacionesCatalogoYSinPrendas() {
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();

        assertThatThrownBy(() -> crearUseCase.crear(usuarioId,
                new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, List.of())))
                .isInstanceOf(PlaylistSinPrendasException.class);

        when(itemCatalogoPort.obtenerPrendaPorId(prendaId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> crearUseCase.crear(usuarioId,
                new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, List.of(prendaId))))
                .isInstanceOf(PrendaNoEncontradaEnCatalogoException.class);

        when(itemCatalogoPort.obtenerPrendaPorId(prendaId))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaId, "Pendiente", false)));
        assertThatThrownBy(() -> crearUseCase.crear(usuarioId,
                new CrearPlaylistRequest("Urbano", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, List.of(prendaId))))
                .isInstanceOf(PrendaNoAprobadaException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void consultasPlaylist() {
        String usuarioId = "15";
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
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(repository.countDestacadas()).thenReturn(3L);

        assertThatThrownBy(() -> destacarUseCase.destacar(usuarioId, playlistId))
                .isInstanceOf(LimiteDestacadasSuperadoException.class);
    }

    @Test
    void asociarYRetirarPrendaValidaCatalogo() {
        UUID usuarioId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.setId(playlistId);

        when(repository.findById(playlistId)).thenReturn(Optional.of(p));
        when(itemCatalogoPort.obtenerPrendaPorId(prendaId))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaId, "Gorra Oficial Catálogo", true)));
        when(repository.save(any())).thenReturn(p);

        PlaylistResponse res = asociarPrendaUseCase.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaId, "Ignorado"));
        assertThat(res.totalPrendas()).isEqualTo(1);
        assertThat(res.prendas().getFirst().nombrePrenda()).isEqualTo("Gorra Oficial Catálogo");

        PlaylistResponse resRetiro = asociarPrendaUseCase.retirarPrenda(usuarioId, playlistId, prendaId);
        assertThat(resRetiro.totalPrendas()).isZero();

        UUID prendaNoExiste = UUID.randomUUID();
        when(itemCatalogoPort.obtenerPrendaPorId(prendaNoExiste)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> asociarPrendaUseCase.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaNoExiste)))
                .isInstanceOf(PrendaNoEncontradaEnCatalogoException.class);

        UUID prendaNoAprobada = UUID.randomUUID();
        when(itemCatalogoPort.obtenerPrendaPorId(prendaNoAprobada))
                .thenReturn(Optional.of(new PrendaCatalogoInfo(prendaNoAprobada, "Pendiente", false)));
        assertThatThrownBy(() -> asociarPrendaUseCase.agregarPrenda(usuarioId, playlistId, new AgregarPrendaRequest(prendaNoAprobada)))
                .isInstanceOf(PrendaNoAprobadaException.class);

        when(repository.findAllConPrenda(prendaId)).thenReturn(List.of(p));
        asociarPrendaUseCase.desvincularPrendaDeTodas(prendaId);
        asociarPrendaUseCase.marcarPrendaNoDisponibleEnTodas(prendaId);
    }

    @Test
    void interactuarLikeYGuardado() {
        String usuarioId = "15";
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
