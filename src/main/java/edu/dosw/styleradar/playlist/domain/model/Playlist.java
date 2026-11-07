package edu.dosw.styleradar.playlist.domain.model;

import edu.dosw.styleradar.playlist.domain.exception.*;

import java.time.Instant;
import java.util.*;

public class Playlist {
    public static final int MAX_PRENDAS = 15; // RNF-12
    public static final int MAX_DESTACADAS = 3; // SR-RF-72

    private Long id;
    private Long usuarioId;
    private String nombre;
    private String descripcion;
    private VisibilidadPlaylist visibilidad;
    private Estilo estilo;
    private boolean destacada;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    private final List<PrendaSnapshot> prendas = new ArrayList<>();
    private final Set<Long> likesUsuarios = new HashSet<>();
    private final Set<Long> guardadosUsuarios = new HashSet<>();

    public Playlist() {
        this.visibilidad = VisibilidadPlaylist.PUBLICA;
        this.estilo = Estilo.URBANO;
        this.destacada = false;
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = Instant.now();
    }

    public Playlist(Long id, Long usuarioId, String nombre, String descripcion,
                    VisibilidadPlaylist visibilidad, Estilo estilo, boolean destacada,
                    Instant fechaCreacion, Instant fechaActualizacion,
                    List<PrendaSnapshot> prendas, Set<Long> likesUsuarios, Set<Long> guardadosUsuarios) {
        this.id = id;
        this.usuarioId = usuarioId;
        setNombre(nombre);
        setDescripcion(descripcion);
        this.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        this.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        this.destacada = destacada;
        this.fechaCreacion = (fechaCreacion != null) ? fechaCreacion : Instant.now();
        this.fechaActualizacion = (fechaActualizacion != null) ? fechaActualizacion : this.fechaCreacion;
        if (prendas != null) {
            if (prendas.size() > MAX_PRENDAS) {
                throw new LimitePrendasAlcanzadoException("La playlist no admite más de " + MAX_PRENDAS + " prendas (RNF-12)");
            }
            this.prendas.addAll(prendas);
        }
        if (likesUsuarios != null) {
            this.likesUsuarios.addAll(likesUsuarios);
        }
        if (guardadosUsuarios != null) {
            this.guardadosUsuarios.addAll(guardadosUsuarios);
        }
    }

    public static Playlist crear(Long usuarioId, String nombre, String descripcion,
                                 VisibilidadPlaylist visibilidad, Estilo estilo) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new DomainException("El ID del usuario creador es obligatorio");
        }
        Playlist p = new Playlist();
        p.usuarioId = usuarioId;
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.visibilidad = (visibilidad != null) ? visibilidad : VisibilidadPlaylist.PUBLICA;
        p.estilo = (estilo != null) ? estilo : Estilo.URBANO;
        p.destacada = false;
        p.fechaCreacion = Instant.now();
        p.fechaActualizacion = p.fechaCreacion;
        return p;
    }

    public void agregarPrenda(PrendaSnapshot prenda) {
        if (prenda == null) {
            throw new DomainException("La prenda no puede ser nula");
        }
        if (this.prendas.size() >= MAX_PRENDAS) {
            throw new LimitePrendasAlcanzadoException("Límite máximo de " + MAX_PRENDAS + " prendas alcanzado");
        }
        boolean yaExiste = this.prendas.stream().anyMatch(p -> p.getPrendaId().equals(prenda.getPrendaId()));
        if (yaExiste) {
            throw new PrendaDuplicadaException("La prenda ya se encuentra asociada a este lookbook");
        }
        this.prendas.add(prenda);
        this.fechaActualizacion = Instant.now();
    }

    public void retirarPrenda(Long prendaId, Long usuarioEjecutorId) {
        validarPropietario(usuarioEjecutorId);
        if (prendaId == null) {
            throw new DomainException("El ID de prenda es obligatorio");
        }
        boolean removida = this.prendas.removeIf(p -> p.getPrendaId().equals(prendaId));
        if (!removida) {
            throw new PrendaNoEncontradaEnPlaylistException("La prenda no pertenece a la playlist");
        }
        this.fechaActualizacion = Instant.now();
    }

    public boolean desvincularPrendaSilencioso(Long prendaId) {
        if (prendaId == null) return false;
        boolean removida = this.prendas.removeIf(p -> p.getPrendaId().equals(prendaId));
        if (removida) {
            this.fechaActualizacion = Instant.now();
        }
        return removida;
    }

    public void destacar() {
        this.destacada = true;
        this.fechaActualizacion = Instant.now();
    }

    public void quitarDestacado() {
        this.destacada = false;
        this.fechaActualizacion = Instant.now();
    }

    public void alternarLike(Long usuarioId) {
        if (usuarioId == null) {
            throw new DomainException("El ID de usuario es obligatorio");
        }
        if (this.likesUsuarios.contains(usuarioId)) {
            this.likesUsuarios.remove(usuarioId);
        } else {
            this.likesUsuarios.add(usuarioId);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void alternarGuardado(Long usuarioId) {
        if (usuarioId == null) {
            throw new DomainException("El ID de usuario es obligatorio");
        }
        if (this.guardadosUsuarios.contains(usuarioId)) {
            this.guardadosUsuarios.remove(usuarioId);
        } else {
            this.guardadosUsuarios.add(usuarioId);
        }
        this.fechaActualizacion = Instant.now();
    }

    public void editar(Long usuarioId, String nombre, String descripcion,
                       VisibilidadPlaylist visibilidad, Estilo estilo) {
        validarPropietario(usuarioId);
        setNombre(nombre);
        setDescripcion(descripcion);
        if (visibilidad != null) {
            this.visibilidad = visibilidad;
        }
        if (estilo != null) {
            this.estilo = estilo;
        }
        this.fechaActualizacion = Instant.now();
    }

    public void validarPropietario(Long usuarioId) {
        if (usuarioId == null || !usuarioId.equals(this.usuarioId)) {
            throw new AccesoDenegadoPlaylistException("No tienes permisos para modificar este lookbook");
        }
    }

    public void validarAccesoLectura(Long usuarioConsultaId) {
        if (this.visibilidad == VisibilidadPlaylist.PRIVADA) {
            if (usuarioConsultaId == null || !usuarioConsultaId.equals(this.usuarioId)) {
                throw new AccesoDenegadoPlaylistException("No tienes permisos para consultar esta colección privada");
            }
        }
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre del lookbook es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new DomainException("El nombre no puede superar los 100 caracteres");
        }
        this.nombre = nombre.trim();
    }

    public void setDescripcion(String descripcion) {
        if (descripcion != null && descripcion.trim().length() > 300) {
            throw new DomainException("La descripción no puede superar los 300 caracteres");
        }
        this.descripcion = (descripcion != null) ? descripcion.trim() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
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
    public Set<Long> getLikesUsuarios() { return Collections.unmodifiableSet(likesUsuarios); }
    public Set<Long> getGuardadosUsuarios() { return Collections.unmodifiableSet(guardadosUsuarios); }

    public int getLikesCount() { return likesUsuarios.size(); }
    public int getGuardadosCount() { return guardadosUsuarios.size(); }
    public int getTotalPrendas() { return prendas.size(); }
}
