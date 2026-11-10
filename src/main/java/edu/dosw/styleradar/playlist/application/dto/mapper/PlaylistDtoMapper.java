package edu.dosw.styleradar.playlist.application.dto.mapper;

import edu.dosw.styleradar.playlist.application.dto.PlaylistResponse;
import edu.dosw.styleradar.playlist.application.dto.PrendaResponse;
import edu.dosw.styleradar.playlist.domain.model.Playlist;
import edu.dosw.styleradar.playlist.domain.model.PrendaSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlaylistDtoMapper {

    @Mapping(target = "likes", expression = "java(domain.getLikesCount())")
    @Mapping(target = "guardados", expression = "java(domain.getGuardadosCount())")
    @Mapping(target = "totalPrendas", expression = "java(domain.getTotalPrendas())")
    PlaylistResponse toResponse(Playlist domain);

    PrendaResponse toPrendaResponse(PrendaSnapshot domain);
}
