package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;

import java.time.Instant;
import java.util.*;

public class Playlist {
    public static final int MAX_PRENDAS = 15; // RNF-12
    public static final int MAX_DESTACADAS = 3; // SR-RF-72

    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private String descripcion;
    private VisibilidadPlaylist visibilidad;
    private Estilo estilo;
    private boolean destacada;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    private final List<PrendaSnapshot> prendas = new ArrayList<>();
    private final Set<UUID> likesUsuarios = new HashSet<>();
    private final Set<UUID> guardadosUsuarios = new HashSet<>();

    public Playlist() {
        this.id = UUID.randomUUID();
        this.visibilidad = VisibilidadPlaylist.PUBLICA;
        this.estilo = Estilo.URBANO;
        this.destacada = false;
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public Playlist(UUID id, UUID usuarioId, String nombre, String descripcion,
                    VisibilidadPlaylist visibilidad, Estilo estilo, boolean destacada,
                    Instant fechaCreacion, Instant fechaActualizacion,
                    List<PrendaSnapshot> prendas, Set<UUID> likesUsuarios, Set<UUID> guardadosUsuarios) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.usuarioId = usuarioId;
        asignarNombre(nombre);
        asignarDescripcion(descripcion);
        this.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        this.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        this.destacada = destacada;
        this.fechaCreacion = (fechaCreacion != null) ? fechaCreacion : Instant.now();
        this.fechaActualizacion = (fechaActualizacion != null) ? fechaActualizacion : this.fechaCreacion;
        asignarPrendas(prendas);
        asignarLikesUsuarios(likesUsuarios);
        asignarGuardadosUsuarios(guardadosUsuarios);
    }

    public static Playlist crear(UUID usuarioId, String nombre, String descripcion,
                                 VisibilidadPlaylist visibilidad, Estilo estilo) {
        if (usuarioId == null) {
            throw new NoAutorizadoException("El ID del usuario creador es obligatorio");
        }
        Playlist p = new Playlist();
        p.id = UUID.randomUUID();
        p.usuarioId = usuarioId;
        p.asignarNombre(nombre);
        p.asignarDescripcion(descripcion);
        p.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        p.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        p.destacada = false;
        p.fechaCreacion = Instant.now();
        p.fechaActualizacion = p.fechaCreacion;
        return p;
    }

    public static Playlist crearConPrendas(UUID usuarioId, String nombre, String descripcion,
                                           VisibilidadPlaylist visibilidad, Estilo estilo,
                                           List<PrendaSnapshot> prendas) {
        Playlist p = crear(usuarioId, nombre, descripcion, visibilidad, estilo);
        if (prendas == null || prendas.isEmpty()) {
            throw new PlaylistSinPrendasException("La playlist debe crearse con al menos una prenda");
        }
        prendas.forEach(p::agregarPrenda);
        return p;
    }

    public void agregarPrenda(PrendaSnapshot prenda) {
        if (prenda == null) {
            throw new DomainException(CodigoError.DATOS_INVALIDOS, "La prenda no puede ser nula");
        }
        if (this.prendas.size() >= MAX_PRENDAS) {
            throw new LimitePrendasAlcanzadoException("Límite máximo de " + MAX_PRENDAS + " prendas alcanzado");
        }
        boolean yaExiste = this.prendas.stream().anyMatch(p -> p.getPrendaId().equals(prenda.getPrendaId()));
        if (yaExiste) {
            throw new PrendaDuplicadaException("La prenda ya se encuentra asociada a esta playlist");
        }
        this.prendas.add(prenda);
        this.fechaActualizacion = Instant.now();
    }

    public void retirarPrenda(UUID prendaId, UUID usuarioEjecutorId) {
        validarPropietario(usuarioEjecutorId);
        if (prendaId == null) {
            throw new DomainException(CodigoError.DATOS_INVALIDOS, "El ID de prenda es obligatorio");
        }
        boolean removida = this.prendas.removeIf(p -> p.getPrendaId().equals(prendaId));
        if (!removida) {
            throw new PrendaNoEncontradaEnPlaylistException("La prenda no pertenece a la playlist");
        }
        this.fechaActualizacion = Instant.now();
    }

    public boolean desvincularPrendaSilencioso(UUID prendaId) {
        if (prendaId == null) return false;
        boolean removida = this.prendas.removeIf(p -> p.getPrendaId().equals(prendaId));
        if (removida) {
            this.fechaActualizacion = Instant.now();
        }
        return removida;
    }

    public boolean marcarPrendaNoDisponible(UUID prendaId) {
        if (prendaId == null) return false;
        boolean encontrada = false;
        for (PrendaSnapshot p : this.prendas) {
            if (p.getPrendaId().equals(prendaId)) {
                p.marcarNoDisponible();
                encontrada = true;
            }
        }
        if (encontrada) {
            this.fechaActualizacion = Instant.now();
        }
        return encontrada;
    }

    public void destacar() {
        this.destacada = true;
        this.fechaActualizacion = Instant.now();
    }

    public void quitarDestacado() {
        this.destacada = false;
        this.fechaActualizacion = Instant.now();
    }

    public void alternarLike(UUID usuarioId) {
        if (usuarioId == null) {
            throw new NoAutorizadoException("El ID de usuario es obligatorio");
        }
        if (this.likesUsuarios.contains(usuarioId)) {
            this.likesUsuarios.remove(usuarioId);
        } else {
            this.likesUsuarios.add(usuarioId);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void alternarGuardado(UUID usuarioId) {
        if (usuarioId == null) {
            throw new NoAutorizadoException("El ID de usuario es obligatorio");
        }
        if (this.guardadosUsuarios.contains(usuarioId)) {
            this.guardadosUsuarios.remove(usuarioId);
        } else {
            this.guardadosUsuarios.add(usuarioId);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void editar(UUID usuarioId, String nombre, String descripcion,
                       VisibilidadPlaylist visibilidad, Estilo estilo) {
        validarPropietario(usuarioId);
        asignarNombre(nombre);
        asignarDescripcion(descripcion);
        if (visibilidad != null) {
            this.visibilidad = visibilidad;
        }
        if (estilo != null) {
            this.estilo = estilo;
        }
        this.fechaActualizacion = Instant.now();
    }

    public void validarPropietario(UUID usuarioId) {
        if (usuarioId == null || !usuarioId.equals(this.usuarioId)) {
            throw new AccesoDenegadoPlaylistException("No tienes permisos para modificar esta playlist");
        }
    }

    public void validarAccesoLectura(UUID usuarioConsultaId) {
        if (this.visibilidad == VisibilidadPlaylist.PRIVADA) {
            if (usuarioConsultaId == null || !usuarioConsultaId.equals(this.usuarioId)) {
                throw new PlaylistPrivadaException("No tienes permisos para consultar esta playlist privada");
            }
        }
    }

    private void asignarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException(CodigoError.DATOS_INVALIDOS, "El nombre de la playlist es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new DomainException(CodigoError.DATOS_INVALIDOS, "El nombre no puede superar los 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    private void asignarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.trim().length() > 300) {
            throw new DescripcionMuyLargaException("La descripción no puede superar los 300 caracteres");
        }
        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
    }

    private void asignarPrendas(List<PrendaSnapshot> prendas) {
        this.prendas.clear();
        if (prendas != null) {
            if (prendas.size() > MAX_PRENDAS) {
                throw new LimitePrendasAlcanzadoException("La playlist no admite más de " + MAX_PRENDAS + " prendas (RNF-12)");
            }
            this.prendas.addAll(prendas);
        }
    }

    private void asignarLikesUsuarios(Set<UUID> likes) {
        this.likesUsuarios.clear();
        if (likes != null) this.likesUsuarios.addAll(likes);
    }

    private void asignarGuardadosUsuarios(Set<UUID> guardados) {
        this.guardadosUsuarios.clear();
        if (guardados != null) this.guardadosUsuarios.addAll(guardados);
    }

    public void setNombre(String nombre) { asignarNombre(nombre); }
    public void setDescripcion(String descripcion) { asignarDescripcion(descripcion); }
    public void setPrendas(List<PrendaSnapshot> prendas) { asignarPrendas(prendas); }
    public void setLikesUsuarios(Set<UUID> likes) { asignarLikesUsuarios(likes); }
    public void setGuardadosUsuarios(Set<UUID> guardados) { asignarGuardadosUsuarios(guardados); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public VisibilidadPlaylist getVisibilidad() { return visibilidad; }
    public void setVisibilidad(VisibilidadPlaylist visibilidad) { this.visibilidad = visibilidad; }
    public Estilo getEstilo() { return estilo; }
    public void setEstilo(Estilo estilo) { this.estilo = estilo; }
    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<PrendaSnapshot> getPrendas() { return Collections.unmodifiableList(prendas); }
    public Set<UUID> getLikesUsuarios() { return Collections.unmodifiableSet(likesUsuarios); }
    public Set<UUID> getGuardadosUsuarios() { return Collections.unmodifiableSet(guardadosUsuarios); }

    public int getLikesCount() { return likesUsuarios.size(); }
    public int getGuardadosCount() { return guardadosUsuarios.size(); }
    public int getTotalPrendas() { return prendas.size(); }
}
