package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.application.port.in.PlaylistUseCasePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalogoEventsConsumerTest {

    @Mock
    private PlaylistUseCasePort playlistUseCase;

    @Test
    void consumeEventoPrendaEliminadaCorrectamente() {
        CatalogoEventsConsumer consumer = new CatalogoEventsConsumer(playlistUseCase);
        consumer.handlePrendaEliminada(Map.of("prendaId", 85L));

        verify(playlistUseCase).desvincularPrendaDeTodas(85L);
    }

    @Test
    void consumeEventoInvalidoNoFalla() {
        CatalogoEventsConsumer consumer = new CatalogoEventsConsumer(playlistUseCase);
        consumer.handlePrendaEliminada(null);

        verifyNoInteractions(playlistUseCase);
    }
}
