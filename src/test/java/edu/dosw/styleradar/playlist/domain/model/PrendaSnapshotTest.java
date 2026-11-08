package edu.dosw.styleradar.playlist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrendaSnapshotTest {

    @Test
    void prendaSnapshotValida() {
        Instant now = Instant.now();
        PrendaSnapshot p1 = new PrendaSnapshot(5L, "Hoodie", now);
        PrendaSnapshot p2 = new PrendaSnapshot(5L, "Otro nombre", now);

        assertThat(p1.getPrendaId()).isEqualTo(5L);
        assertThat(p1.getNombrePrenda()).isEqualTo("Hoodie");
        assertThat(p1.getFechaAsociacion()).isEqualTo(now);
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void prendaSnapshotInvalidaLanzaExcepcion() {
        assertThatThrownBy(() -> new PrendaSnapshot(0L, "Hoodie", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
