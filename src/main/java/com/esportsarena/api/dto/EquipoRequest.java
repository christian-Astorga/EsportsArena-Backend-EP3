package com.esportsarena.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipoRequest(
        @NotNull Long juegoId,
        @NotBlank @Size(min = 3, max = 80) String nombre,
        @NotNull @Min(1) Integer integrantes,
        @NotBlank @Size(min = 6, max = 72, message = "El PIN debe tener al menos 6 caracteres") String pin) {}
