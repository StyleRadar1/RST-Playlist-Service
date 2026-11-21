package edu.dosw.styleradar.playlist.infrastructure.adapter.out.external;

import edu.dosw.styleradar.playlist.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.playlist.domain.port.out.ItemCatalogoPort;
import edu.dosw.styleradar.playlist.domain.port.out.PrendaCatalogoInfo;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ItemCatalogoClient implements ItemCatalogoPort {

    private static final Logger log = LoggerFactory.getLogger(ItemCatalogoClient.class);

    private final ItemCatalogoFeignClient feignClient;

    public ItemCatalogoClient(ItemCatalogoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public Optional<PrendaCatalogoInfo> obtenerPrendaPorId(UUID prendaId) {
        if (prendaId == null) {
            return Optional.empty();
        }
        try {
            PrendaCatalogoDto dto = feignClient.obtenerPrenda(prendaId);
            if (dto == null) {
                return Optional.empty();
            }
            UUID id = dto.id() != null ? dto.id() : prendaId;
            return Optional.of(new PrendaCatalogoInfo(id, dto.nombre(), dto.estaAprobada()));
        } catch (FeignException.NotFound ex) {
            log.debug("Prenda {} no encontrada en ms-item-catalogo: {}", prendaId, ex.getMessage());
            return Optional.empty();
        } catch (FeignException ex) {
            log.error("Error comunicándose con ms-item-catalogo al consultar prenda {}: status={} msg={}",
                    prendaId, ex.status(), ex.getMessage());
            throw new CatalogoNoDisponibleException(
                    "El servicio de catálogo (ms-item-catalogo) no está disponible", ex);
        }
    }
}
