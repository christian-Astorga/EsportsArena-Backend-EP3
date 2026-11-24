package com.esportsarena.api.dto;

/** Nunca incluye el PIN (ni su hash). */
public record EquipoResponse(
        Long id,
        Long juegoId,
        String juegoNombre,
        String nombre,
        int integrantes,
        Long capitanId,
        String capitanNombre) {}
