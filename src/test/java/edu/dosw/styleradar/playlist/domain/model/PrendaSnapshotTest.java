package edu.dosw.styleradar.playlist.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrendaSnapshotTest {

    @Test
    void prendaSnapshotValidaYDisponibilidad() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        PrendaSnapshot p1 = new PrendaSnapshot(id, "Hoodie", now);

        assertThat(p1.getPrendaId()).isEqualTo(id);
        assertThat(p1.getNombrePrenda()).isEqualTo("Hoodie");
        assertThat(p1.getFechaAsociacion()).isEqualTo(now);
        assertThat(p1.isDisponible()).isTrue();
        assertThat(p1.necesitaEnriquecimiento()).isTrue();

        p1.completarDatosCatalogo(" https://cdn.example/hoodie.jpg ", new BigDecimal("129000"));
        assertThat(p1.getImagenUrl()).isEqualTo("https://cdn.example/hoodie.jpg");
        assertThat(p1.getPrecio()).isEqualByComparingTo("129000");
        assertThat(p1.necesitaEnriquecimiento()).isFalse();

        // No sobrescribe datos existentes
        p1.completarDatosCatalogo("https://cdn.example/otra.jpg", new BigDecimal("1"));
        assertThat(p1.getImagenUrl()).isEqualTo("https://cdn.example/hoodie.jpg");
        assertThat(p1.getPrecio()).isEqualByComparingTo("129000");

        p1.setImagenUrl("   ");
        assertThat(p1.getImagenUrl()).isNull();
        p1.setPrecio(new BigDecimal("135000"));
        assertThat(p1.getPrecio()).isEqualByComparingTo("135000");

        p1.marcarNoDisponible();
        assertThat(p1.isDisponible()).isFalse();

        p1.marcarDisponible();
        assertThat(p1.isDisponible()).isTrue();

        p1.setDisponible(false);
        assertThat(p1.isDisponible()).isFalse();
    }

    @Test
    void prendaSnapshotInvalidaLanzaExcepcion() {
        assertThatThrownBy(() -> new PrendaSnapshot(null, "Hoodie", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
