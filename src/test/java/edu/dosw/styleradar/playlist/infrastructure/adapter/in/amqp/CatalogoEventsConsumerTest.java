package edu.dosw.styleradar.playlist.infrastructure.adapter.in.amqp;

import edu.dosw.styleradar.playlist.domain.port.in.AsociarPrendaUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CatalogoEventsConsumerTest {

    @Mock
    private AsociarPrendaUseCase asociarPrendaUseCase;

    @Test
    void consumeEventoPrendaEliminadaCorrectamente() {
        UUID prendaId = UUID.randomUUID();
        CatalogoEventsConsumer consumer = new CatalogoEventsConsumer(asociarPrendaUseCase);
        consumer.handlePrendaEliminada(Map.of("prendaId", prendaId.toString()));

        verify(asociarPrendaUseCase).desvincularPrendaDeTodas(prendaId);
    }

    @Test
    void consumeEventoInvalidoNoFalla() {
        CatalogoEventsConsumer consumer = new CatalogoEventsConsumer(asociarPrendaUseCase);
        consumer.handlePrendaEliminada(null);
        consumer.handlePrendaEliminada(Map.of("prendaId", "no-es-uuid"));

        verifyNoInteractions(asociarPrendaUseCase);
    }
}
