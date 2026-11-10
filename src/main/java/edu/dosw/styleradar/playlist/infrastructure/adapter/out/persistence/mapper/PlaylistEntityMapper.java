package edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.mapper;

import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PlaylistEntity;
import edu.dosw.styleradar.playlist.infrastructure.adapter.out.persistence.entity.PrendaEmbeddable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaylistEntityMapper {

    Playlist toDomain(PlaylistEntity entity);

    PlaylistEntity toEntity(Playlist domain);

    PrendaSnapshot toPrendaDomain(PrendaEmbeddable embeddable);

    PrendaEmbeddable toPrendaEmbeddable(PrendaSnapshot domain);
}
