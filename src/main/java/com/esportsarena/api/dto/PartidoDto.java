package com.esportsarena.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PartidoDto(
        @NotBlank String ronda,
        @NotBlank String partido,
        @NotNull LocalDate fecha) {}
