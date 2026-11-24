package com.esportsarena.api.dto;

import java.time.LocalDateTime;

public record InscripcionResponse(
        Long id,
        Long torneoId,
        String torneoNombre,
        Long usuarioId,
        String usuarioNombre,
        String correo,
        int edad,
        LocalDateTime fecha) {}
