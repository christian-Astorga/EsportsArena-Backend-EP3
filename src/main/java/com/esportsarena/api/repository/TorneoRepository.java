package com.esportsarena.api.repository;

import com.esportsarena.api.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByJuegoId(Long juegoId);
}
