package com.esportsarena.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InscripcionRequest(
        @NotNull Long torneoId,
        @NotBlank @Email String correo,
        @NotNull @Min(value = 14, message = "La edad mínima es 14 años") @Max(value = 99, message = "La edad máxima es 99 años") Integer edad) {}
