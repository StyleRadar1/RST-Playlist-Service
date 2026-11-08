package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.playlist.domain.model.VisibilidadPlaylist;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataPlaylistRepository extends JpaRepository<PlaylistEntity, Long> {

    List<PlaylistEntity> findByVisibilidadOrderByFechaCreacionDesc(VisibilidadPlaylist visibilidad);

    List<PlaylistEntity> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<PlaylistEntity> findByGuardadosUsuariosContainsOrderByFechaCreacionDesc(Long usuarioId);

    List<PlaylistEntity> findByDestacadaTrueOrderByFechaActualizacionDesc();

    long countByDestacadaTrue();

    long countByVisibilidad(VisibilidadPlaylist visibilidad);

    @Query("SELECT p FROM PlaylistEntity p WHERE p.visibilidad = 'PUBLICA' ORDER BY SIZE(p.likesUsuarios) DESC, p.fechaCreacion DESC")
    Page<PlaylistEntity> findPopulares(Pageable pageable);

    @Query("SELECT DISTINCT p FROM PlaylistEntity p JOIN p.prendas pr WHERE pr.prendaId = :prendaId")
    List<PlaylistEntity> findAllByPrendaId(@Param("prendaId") Long prendaId);
}
