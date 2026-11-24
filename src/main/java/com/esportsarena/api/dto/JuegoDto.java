package com.esportsarena.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Se usa como entrada y como salida (el id se ignora al crear). */
public record JuegoDto(
        Long id,
        @NotBlank @Size(max = 80) String nombre,
        @NotNull @Min(1) Integer minIntegrantes) {}
