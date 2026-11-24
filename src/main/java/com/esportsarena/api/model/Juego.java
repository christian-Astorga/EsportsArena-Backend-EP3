package com.esportsarena.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Equivale a "Categoría" del modelo propuesto en la guía. */
@Entity
@Table(name = "juegos", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private int minIntegrantes;

    public Juego(String nombre, int minIntegrantes) {
        this.nombre = nombre;
        this.minIntegrantes = minIntegrantes;
    }
}
