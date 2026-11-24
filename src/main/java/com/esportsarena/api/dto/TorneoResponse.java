package com.esportsarena.api.dto;

import com.esportsarena.api.model.EstadoTorneo;
import java.time.LocalDate;
import java.util.List;

public record TorneoResponse(
        Long id,
        String nombre,
        String descripcion,
        Long juegoId,
        String juegoNombre,
        EstadoTorneo estado,
        boolean destacado,
        int cuposMaximos,
        int cuposOcupados,
        int cuposDisponibles,
        LocalDate fechaCierre,
        List<String> imagenes,
        List<String> premios,
        List<PartidoDto> calendario,
        List<String> llaves) {}
