package com.esportsarena.api.repository;

import com.esportsarena.api.model.Juego;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuegoRepository extends JpaRepository<Juego, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
