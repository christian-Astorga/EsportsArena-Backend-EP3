package com.esportsarena.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** En JSON se usa la etiqueta legible (la misma que usa el frontend React). */
public enum EstadoTorneo {
    INSCRIPCIONES_ABIERTAS("Inscripciones Abiertas"),
    EN_CURSO("En Curso"),
    FINALIZADO("Finalizado");

    private final String etiqueta;

    EstadoTorneo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String getEtiqueta() {
        return etiqueta;
    }

    /** Acepta tanto la etiqueta ("En Curso") como el nombre del enum ("EN_CURSO"). */
    @JsonCreator
    public static EstadoTorneo desde(String valor) {
        if (valor == null) return null;
        for (EstadoTorneo e : values()) {
            if (e.etiqueta.equalsIgnoreCase(valor) || e.name().equalsIgnoreCase(valor)) return e;
        }
        throw new IllegalArgumentException("Estado de torneo no válido: " + valor);
    }
}
