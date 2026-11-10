package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.playlist.domain.model.Estilo;
import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "playlists")
public class PlaylistEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisibilidadPlaylist visibilidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estilo estilo;

    @Column(nullable = false)
    private boolean destacada;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "playlist_prendas", joinColumns = @JoinColumn(name = "playlist_id"))
    private List<PrendaEmbeddable> prendas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "playlist_likes", joinColumns = @JoinColumn(name = "playlist_id"))
    @Column(name = "usuario_id", nullable = false)
    private Set<UUID> likesUsuarios = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "playlist_guardados", joinColumns = @JoinColumn(name = "playlist_id"))
    @Column(name = "usuario_id", nullable = false)
    private Set<UUID> guardadosUsuarios = new HashSet<>();

    public PlaylistEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
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
    public List<PrendaEmbeddable> getPrendas() { return prendas; }
    public void setPrendas(List<PrendaEmbeddable> prendas) { this.prendas = prendas; }
    public Set<UUID> getLikesUsuarios() { return likesUsuarios; }
    public void setLikesUsuarios(Set<UUID> likesUsuarios) { this.likesUsuarios = likesUsuarios; }
    public Set<UUID> getGuardadosUsuarios() { return guardadosUsuarios; }
    public void setGuardadosUsuarios(Set<UUID> guardadosUsuarios) { this.guardadosUsuarios = guardadosUsuarios; }
}
