package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaylistTest {

    @Test
    void crearPlaylistValidaConUsuarioUuid() {
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Look Urbano Bogotá", "Hoodies y joggers", VisibilidadPlaylist.PUBLICA, Estilo.URBANO);

        assertThat(p.getId()).isNotNull();
        assertThat(p.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(p.getNombre()).isEqualTo("Look Urbano Bogotá");
        assertThat(p.getDescripcion()).isEqualTo("Hoodies y joggers");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PUBLICA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.URBANO);
        assertThat(p.isDestacada()).isFalse();
        assertThat(p.getTotalPrendas()).isZero();
        assertThat(p.getLikesCount()).isZero();
    }

    @Test
    void crearConPrendasValidaMinimoUnaPrenda() {
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crearConPrendas(
                usuarioId, "Look", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO,
                List.of(new PrendaSnapshot(prendaId, "Hoodie", Instant.now())));

        assertThat(p.getTotalPrendas()).isEqualTo(1);

        assertThatThrownBy(() -> Playlist.crearConPrendas(
                usuarioId, "Look", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, List.of()))
                .isInstanceOf(PlaylistSinPrendasException.class)
                .extracting(e -> ((DomainException) e).getCodigoError())
                .isEqualTo(CodigoError.PLAYLIST_SIN_PRENDAS);

        assertThatThrownBy(() -> Playlist.crearConPrendas(
                usuarioId, "Look", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.URBANO, null))
                .isInstanceOf(PlaylistSinPrendasException.class);
    }

    @Test
    void constructorPorDefecto() {
        Playlist p = new Playlist();
        assertThat(p.getId()).isNotNull();
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PUBLICA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.URBANO);
        assertThat(p.isDestacada()).isFalse();
    }

    @Test
    void crearPlaylistUsuarioNuloLanzaExcepcion() {
        assertThatThrownBy(() -> Playlist.crear(null, "Test", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL))
                .isInstanceOf(NoAutorizadoException.class);
    }

    @Test
    void setNombreVacioOLargoLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        assertThatThrownBy(() -> p.setNombre(""))
                .isInstanceOf(DomainException.class);
        String largo = "a".repeat(101);
        assertThatThrownBy(() -> p.setNombre(largo))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void setDescripcionLargaLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Inicial", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        String largo = "a".repeat(301);

        assertThatThrownBy(() -> p.setDescripcion(largo))
                .isInstanceOf(DescripcionMuyLargaException.class)
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCodigoError())
                .isEqualTo(CodigoError.DESCRIPCION_MUY_LARGA);
    }

    @Test
    void agregarPrendaHasta15Exitoso() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);

        for (int i = 1; i <= 15; i++) {
            p.agregarPrenda(new PrendaSnapshot(UUID.randomUUID(), "Prenda " + i, Instant.now()));
        }

        assertThat(p.getTotalPrendas()).isEqualTo(15);
        PrendaSnapshot extra = new PrendaSnapshot(UUID.randomUUID(), "Prenda 16", Instant.now());
        assertThatThrownBy(() -> p.agregarPrenda(extra))
                .isInstanceOf(LimitePrendasAlcanzadoException.class);
    }

    @Test
    void agregarPrendaNulaLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        assertThatThrownBy(() -> p.agregarPrenda(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void agregarPrendaDuplicadaLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        UUID prendaId = UUID.randomUUID();
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Chaqueta", Instant.now()));
        PrendaSnapshot duplicada = new PrendaSnapshot(prendaId, "Chaqueta Otra", Instant.now());

        assertThatThrownBy(() -> p.agregarPrenda(duplicada))
                .isInstanceOf(PrendaDuplicadaException.class);
    }

    @Test
    void retirarPrendaPorPropietario() {
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        p.retirarPrenda(prendaId, usuarioId);
        assertThat(p.getTotalPrendas()).isZero();

        assertThatThrownBy(() -> p.retirarPrenda(prendaId, usuarioId))
                .isInstanceOf(PrendaNoEncontradaEnPlaylistException.class);
    }

    @Test
    void desvincularPrendaSilencioso() {
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        boolean removida = p.desvincularPrendaSilencioso(prendaId);
        assertThat(removida).isTrue();
        assertThat(p.getTotalPrendas()).isZero();

        boolean segunda = p.desvincularPrendaSilencioso(prendaId);
        assertThat(segunda).isFalse();
        assertThat(p.desvincularPrendaSilencioso(null)).isFalse();
    }

    @Test
    void marcarPrendaNoDisponibleEnPlaylist() {
        UUID prendaId = UUID.randomUUID();
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.agregarPrenda(new PrendaSnapshot(prendaId, "Pantalón", Instant.now()));

        assertThat(p.getPrendas().get(0).isDisponible()).isTrue();
        boolean marcada = p.marcarPrendaNoDisponible(prendaId);
        assertThat(marcada).isTrue();
        assertThat(p.getPrendas().get(0).isDisponible()).isFalse();

        assertThat(p.marcarPrendaNoDisponible(null)).isFalse();
        assertThat(p.marcarPrendaNoDisponible(UUID.randomUUID())).isFalse();
    }

    @Test
    void alternarLikeYGuardado() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        p.alternarLike(u1);
        assertThat(p.getLikesCount()).isEqualTo(1);
        p.alternarLike(u1);
        assertThat(p.getLikesCount()).isZero();

        p.alternarGuardado(u2);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
        p.alternarGuardado(u2);
        assertThat(p.getGuardadosCount()).isZero();

        assertThatThrownBy(() -> p.alternarLike(null)).isInstanceOf(NoAutorizadoException.class);
        assertThatThrownBy(() -> p.alternarGuardado(null)).isInstanceOf(NoAutorizadoException.class);
    }

    @Test
    void destacarYQuitarDestacado() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección", null, VisibilidadPlaylist.PUBLICA, Estilo.CASUAL);
        p.destacar();
        assertThat(p.isDestacada()).isTrue();
        p.quitarDestacado();
        assertThat(p.isDestacada()).isFalse();
    }

    @Test
    void validarAccesoLecturaPrivada() {
        UUID usuarioId = UUID.randomUUID();
        UUID ajeno = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Colección Privada", null, VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        p.validarAccesoLectura(usuarioId);
        assertThatThrownBy(() -> p.validarAccesoLectura(ajeno))
                .isInstanceOf(PlaylistPrivadaException.class)
                .isInstanceOf(AccesoDenegadoPlaylistException.class)
                .extracting(e -> ((DomainException) e).getCodigoError())
                .isEqualTo(CodigoError.PLAYLIST_PRIVADA);
    }

    @Test
    void validarAccesoLecturaPublica() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Colección Pública", null, VisibilidadPlaylist.PUBLICA, Estilo.FORMAL);
        p.validarAccesoLectura(null);
        p.validarAccesoLectura(UUID.randomUUID());
        assertThat(p.getNombre()).isEqualTo("Colección Pública");
    }

    @Test
    void editarPlaylistExitoso() {
        UUID usuarioId = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        p.editar(usuarioId, "Modificado", "Desc", VisibilidadPlaylist.PRIVADA, Estilo.FORMAL);

        assertThat(p.getNombre()).isEqualTo("Modificado");
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PRIVADA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.FORMAL);
    }

    @Test
    void editarPlaylistUsuarioAjenoLanzaExcepcion() {
        Playlist p = Playlist.crear(UUID.randomUUID(), "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        UUID ajeno = UUID.randomUUID();
        assertThatThrownBy(() -> p.editar(ajeno, "Modificado", null, null, null))
                .isInstanceOf(AccesoDenegadoPlaylistException.class)
                .extracting(e -> ((DomainException) e).getCodigoError())
                .isEqualTo(CodigoError.ACCESO_DENEGADO);
    }

    @Test
    void retirarPrendaInvalidaLanzaExcepciones() {
        UUID usuarioId = UUID.randomUUID();
        UUID ajeno = UUID.randomUUID();
        Playlist p = Playlist.crear(usuarioId, "Original", null, VisibilidadPlaylist.PUBLICA, Estilo.URBANO);
        UUID prendaId = UUID.randomUUID();
        assertThatThrownBy(() -> p.retirarPrenda(prendaId, ajeno))
                .isInstanceOf(AccesoDenegadoPlaylistException.class);
        assertThatThrownBy(() -> p.retirarPrenda(null, usuarioId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void constructorCompletoConValidaciones() {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID prendaId = UUID.randomUUID();
        UUID likeUser = UUID.randomUUID();
        UUID guardUser = UUID.randomUUID();

        Playlist p = new Playlist(
                id, usuarioId, "Nombre", "Desc", VisibilidadPlaylist.PUBLICA, Estilo.DEPORTIVO, true,
                Instant.now(), Instant.now(), List.of(new PrendaSnapshot(prendaId, "p", Instant.now())),
                Set.of(likeUser), Set.of(guardUser)
        );

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(p.isDestacada()).isTrue();
        assertThat(p.getTotalPrendas()).isEqualTo(1);
        assertThat(p.getLikesCount()).isEqualTo(1);
        assertThat(p.getGuardadosCount()).isEqualTo(1);
    }

    @Test
    void settersYGetters() {
        Playlist p = new Playlist();
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        p.setId(id);
        p.setUsuarioId(usuarioId);
        p.setVisibilidad(VisibilidadPlaylist.PRIVADA);
        p.setEstilo(Estilo.FORMAL);
        p.setDestacada(true);
        p.setFechaActualizacion(Instant.now());

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(p.getVisibilidad()).isEqualTo(VisibilidadPlaylist.PRIVADA);
        assertThat(p.getEstilo()).isEqualTo(Estilo.FORMAL);
        assertThat(p.isDestacada()).isTrue();
    }
}
