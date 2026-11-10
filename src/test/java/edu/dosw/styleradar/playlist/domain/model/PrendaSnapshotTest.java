package edu.dosw.styleradar.playlist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrendaSnapshotTest {

    @Test
    void prendaSnapshotValida() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        PrendaSnapshot p1 = new PrendaSnapshot(id, "Hoodie", now);
        PrendaSnapshot p2 = new PrendaSnapshot(id, "Otro nombre", now);

        assertThat(p1.getPrendaId()).isEqualTo(id);
        assertThat(p1.getNombrePrenda()).isEqualTo("Hoodie");
        assertThat(p1.getFechaAsociacion()).isEqualTo(now);
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void prendaSnapshotInvalidaLanzaExcepcion() {
        assertThatThrownBy(() -> new PrendaSnapshot(null, "Hoodie", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
