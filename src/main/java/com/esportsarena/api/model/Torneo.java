package com.esportsarena.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Equivale a "Producto" del modelo propuesto en la guía. */
@Entity
@Table(name = "torneos")
@Getter
@Setter
@NoArgsConstructor
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoTorneo estado;

    @Column(nullable = false)
    private boolean destacado;

    @Column(nullable = false)
    private int cuposMaximos;

    @Column(nullable = false)
    private int cuposOcupados;

    @Column(nullable = false)
    private LocalDate fechaCierre;

    @ElementCollection
    @CollectionTable(name = "torneo_imagenes", joinColumns = @JoinColumn(name = "torneo_id"))
    @OrderColumn(name = "orden")
    @Column(name = "url", length = 300)
    private List<String> imagenes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "torneo_premios", joinColumns = @JoinColumn(name = "torneo_id"))
    @OrderColumn(name = "orden")
    @Column(name = "premio", length = 200)
    private List<String> premios = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "torneo_llaves", joinColumns = @JoinColumn(name = "torneo_id"))
    @OrderColumn(name = "orden")
    @Column(name = "llave", length = 200)
    private List<String> llaves = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "torneo_calendario", joinColumns = @JoinColumn(name = "torneo_id"))
    @OrderColumn(name = "orden")
    private List<PartidoCalendario> calendario = new ArrayList<>();

    /** Bloqueo optimista: evita que dos inscripciones simultáneas sobrepasen los cupos. */
    @Version
    private Long version;
}
