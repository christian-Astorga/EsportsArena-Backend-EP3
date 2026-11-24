package com.esportsarena.api.repository;

import com.esportsarena.api.model.Inscripcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    boolean existsByUsuarioIdAndTorneoId(Long usuarioId, Long torneoId);

    boolean existsByTorneoId(Long torneoId);

    boolean existsByUsuarioId(Long usuarioId);

    List<Inscripcion> findByUsuarioCorreoOrderByFechaDesc(String correo);
}
