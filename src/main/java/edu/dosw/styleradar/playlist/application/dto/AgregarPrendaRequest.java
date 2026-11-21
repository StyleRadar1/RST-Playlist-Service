package edu.dosw.styleradar.playlist.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AgregarPrendaRequest(
    @NotNull(message = "El ID de la prenda es obligatorio")
    UUID prendaId,

    @Schema(description = "Campo opcional legado; el nombre oficial se consulta en ms-item-catalogo",
            deprecated = true)
    String nombrePrenda
) {
    public AgregarPrendaRequest(UUID prendaId) {
        this(prendaId, null);
    }
}
