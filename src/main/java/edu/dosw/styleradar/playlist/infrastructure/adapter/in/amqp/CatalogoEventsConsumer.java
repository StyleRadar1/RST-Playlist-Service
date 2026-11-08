package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.application.port.in.PlaylistUseCasePort;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CatalogoEventsConsumer {

    private final PlaylistUseCasePort playlistUseCase;

    public CatalogoEventsConsumer(PlaylistUseCasePort playlistUseCase) {
        this.playlistUseCase = playlistUseCase;
    }

    @RabbitListener(queues = "${styleradar.rabbitmq.queues.prenda-eliminada:q.playlist.prenda-eliminada}")
    public void handlePrendaEliminada(Map<String, Object> event) {
        if (event != null && event.get("prendaId") != null) {
            try {
                Long prendaId = Long.valueOf(event.get("prendaId").toString());
                playlistUseCase.desvincularPrendaDeTodas(prendaId);
            } catch (NumberFormatException ignored) {}
        }
    }
}
