package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CatalogoEventsConsumer {

    private final AsociarPrendaUseCase asociarPrendaUseCase;

    public CatalogoEventsConsumer(AsociarPrendaUseCase asociarPrendaUseCase) {
        this.asociarPrendaUseCase = asociarPrendaUseCase;
    }

    @RabbitListener(queues = "${styleradar.rabbitmq.queues.prenda-retirada:styleradar.playlist.prenda-retirada.q}")
    public void handlePrendaRetirada(Map<String, Object> event) {
        if (event != null && event.get("prendaId") != null) {
            try {
                UUID prendaId = UUID.fromString(event.get("prendaId").toString());
                asociarPrendaUseCase.marcarPrendaNoDisponibleEnTodas(prendaId);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
