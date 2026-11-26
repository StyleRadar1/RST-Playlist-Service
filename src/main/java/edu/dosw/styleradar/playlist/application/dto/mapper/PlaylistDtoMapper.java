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
    @Mapping(target = "liked", constant = "false")
    @Mapping(target = "saved", constant = "false")
    PlaylistResponse toResponse(Playlist domain);

    default PlaylistResponse toResponse(Playlist domain, String consultanteId) {
        PlaylistResponse base = toResponse(domain);
        if (base == null || consultanteId == null || consultanteId.isBlank()) {
            return base;
        }
        return new PlaylistResponse(base.id(), base.usuarioId(), base.nombre(), base.descripcion(), base.visibilidad(),
                base.estilo(), base.destacada(), base.fechaCreacion(), base.prendas(), base.likes(), base.guardados(),
                base.totalPrendas(), domain.tieneLikeDe(consultanteId), domain.estaGuardadaPor(consultanteId));
    }

    PrendaResponse toPrendaResponse(PrendaSnapshot domain);
}
