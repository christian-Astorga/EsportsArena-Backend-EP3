package com.esportsarena.api.dto;

import com.esportsarena.api.model.EstadoTorneo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record TorneoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull Long juegoId,
        @NotNull EstadoTorneo estado,
        Boolean destacado,
        @NotNull @Min(1) Integer cuposMaximos,
        @NotNull LocalDate fechaCierre,
        List<@NotBlank String> imagenes,
        List<@NotBlank String> premios,
        List<@NotBlank String> llaves,
        @Valid List<PartidoDto> calendario) {}
