package com.esportsarena.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Equivale a "Boleta" (y su detalle) del modelo de la guía: un usuario se inscribe a un torneo. */
@Entity
@Table(name = "inscripciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "torneo_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(nullable = false, length = 150)
    private String correoContacto;

    @Column(nullable = false)
    private int edad;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
