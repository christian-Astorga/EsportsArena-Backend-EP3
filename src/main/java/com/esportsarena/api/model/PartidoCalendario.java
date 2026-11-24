package com.esportsarena.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartidoCalendario {

    @Column(length = 80)
    private String ronda;

    @Column(length = 120)
    private String partido;

    private LocalDate fecha;
}
