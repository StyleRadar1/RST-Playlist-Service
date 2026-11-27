package edu.dosw.styleradar.playlist.infrastructure.adapter.out.external;

import edu.dosw.styleradar.playlist.domain.exception.CatalogoNoDisponibleException;
import edu.dosw.styleradar.playlist.domain.port.out.PrendaCatalogoInfo;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCatalogoClientTest {

    @Mock
    private ItemCatalogoFeignClient feignClient;

    @InjectMocks
    private ItemCatalogoClient client;

    @Test
    void retornaPrendaAprobadaONoAprobada() {
        UUID prendaId = UUID.randomUUID();
        when(feignClient.obtenerPrenda(prendaId))
                .thenReturn(new PrendaCatalogoDto(prendaId, "Hoodie Oversize", "APROBADA", null));

        Optional<PrendaCatalogoInfo> info = client.obtenerPrendaPorId(prendaId);
        assertThat(info).isPresent();
        assertThat(info.get().id()).isEqualTo(prendaId);
        assertThat(info.get().nombre()).isEqualTo("Hoodie Oversize");
        assertThat(info.get().aprobada()).isTrue();

        when(feignClient.obtenerPrenda(prendaId))
                .thenReturn(new PrendaCatalogoDto(null, "Chaqueta", "PENDIENTE", true));
        Optional<PrendaCatalogoInfo> pendiente = client.obtenerPrendaPorId(prendaId);
        assertThat(pendiente).isPresent();
        assertThat(pendiente.get().id()).isEqualTo(prendaId);
        assertThat(pendiente.get().aprobada()).isFalse();

        when(feignClient.obtenerPrenda(prendaId))
                .thenReturn(new PrendaCatalogoDto(prendaId, "Gorra", null, true));
        assertThat(client.obtenerPrendaPorId(prendaId).orElseThrow().aprobada()).isTrue();

        when(feignClient.obtenerPrenda(prendaId))
                .thenReturn(new PrendaCatalogoDto(
                        prendaId,
                        "Hoodie gris jaspeado",
                        "APROBADA",
                        null,
                        List.of(" ", "https://cdn.example/hoodie.jpg"),
                        "https://cdn.example/fallback.jpg",
                        new BigDecimal("129000.00")));
        PrendaCatalogoInfo conFotoYPrecio = client.obtenerPrendaPorId(prendaId).orElseThrow();
        assertThat(conFotoYPrecio.imagenUrl()).isEqualTo("https://cdn.example/hoodie.jpg");
        assertThat(conFotoYPrecio.precio()).isEqualByComparingTo("129000.00");

        when(feignClient.obtenerPrenda(prendaId))
                .thenReturn(new PrendaCatalogoDto(
                        prendaId,
                        "Camisa",
                        "APROBADA",
                        null,
                        List.of(),
                        " https://cdn.example/camisa.jpg ",
                        new BigDecimal("95000")));
        assertThat(client.obtenerPrendaPorId(prendaId).orElseThrow().imagenUrl())
                .isEqualTo("https://cdn.example/camisa.jpg");
    }

    @Test
    void retornaVacioSiNuloO404() {
        assertThat(client.obtenerPrendaPorId(null)).isEmpty();
        verifyNoInteractions(feignClient);

        UUID prendaId = UUID.randomUUID();
        when(feignClient.obtenerPrenda(prendaId)).thenReturn(null);
        assertThat(client.obtenerPrendaPorId(prendaId)).isEmpty();

        Request req = Request.create(Request.HttpMethod.GET, "/api/v1/garments/" + prendaId,
                Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException notFound = FeignException.errorStatus(
                "ItemCatalogoFeignClient#obtenerPrenda(UUID)",
                Response.builder().status(404).reason("Not Found").request(req).headers(Map.of()).build());
        when(feignClient.obtenerPrenda(prendaId)).thenThrow(notFound);

        assertThat(client.obtenerPrendaPorId(prendaId)).isEmpty();
    }

    @Test
    void lanzaCatalogoNoDisponibleEnErrorDeRed() {
        UUID prendaId = UUID.randomUUID();
        Request req = Request.create(Request.HttpMethod.GET, "/api/v1/garments/" + prendaId,
                Map.of(), null, StandardCharsets.UTF_8, null);
        FeignException unavailable = FeignException.errorStatus(
                "ItemCatalogoFeignClient#obtenerPrenda(UUID)",
                Response.builder().status(503).reason("Service Unavailable").request(req).headers(Map.of()).build());

        when(feignClient.obtenerPrenda(prendaId)).thenThrow(unavailable);

        assertThatThrownBy(() -> client.obtenerPrendaPorId(prendaId))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }
}
