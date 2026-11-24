package com.esportsarena.api.repository;

import com.esportsarena.api.model.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByCapitanCorreo(String correo);

    boolean existsByJuegoId(Long juegoId);

    boolean existsByCapitanId(Long usuarioId);
}
