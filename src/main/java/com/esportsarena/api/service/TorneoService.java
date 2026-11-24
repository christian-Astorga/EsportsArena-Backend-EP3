package com.esportsarena.api.service;

import com.esportsarena.api.dto.PartidoDto;
import com.esportsarena.api.dto.TorneoRequest;
import com.esportsarena.api.dto.TorneoResponse;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.exception.ResourceNotFoundException;
import com.esportsarena.api.model.Juego;
import com.esportsarena.api.model.PartidoCalendario;
import com.esportsarena.api.model.Torneo;
import com.esportsarena.api.repository.InscripcionRepository;
import com.esportsarena.api.repository.JuegoRepository;
import com.esportsarena.api.repository.TorneoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final JuegoRepository juegoRepository;
    private final InscripcionRepository inscripcionRepository;

    public TorneoService(TorneoRepository torneoRepository, JuegoRepository juegoRepository,
                         InscripcionRepository inscripcionRepository) {
        this.torneoRepository = torneoRepository;
        this.juegoRepository = juegoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional(readOnly = true)
    public List<TorneoResponse> listar() {
        return torneoRepository.findAll().stream().map(TorneoService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public TorneoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public TorneoResponse crear(TorneoRequest r) {
        Torneo t = new Torneo();
        t.setCuposOcupados(0);
        aplicar(t, r);
        return aRespuesta(torneoRepository.save(t));
    }

    @Transactional
    public TorneoResponse actualizar(Long id, TorneoRequest r) {
        Torneo t = buscar(id);
        aplicar(t, r);
        if (t.getCuposMaximos() < t.getCuposOcupados()) {
            throw new BusinessException("Los cupos máximos (" + t.getCuposMaximos()
                    + ") no pueden ser menores a los cupos ya ocupados (" + t.getCuposOcupados() + ")");
        }
        return aRespuesta(torneoRepository.save(t));
    }

    @Transactional
    public void eliminar(Long id) {
        Torneo t = buscar(id);
        if (inscripcionRepository.existsByTorneoId(id)) {
            throw new BusinessException("No se puede eliminar un torneo que ya tiene inscripciones");
        }
        torneoRepository.delete(t);
    }

    // ---------- helpers ----------
    private Torneo buscar(Long id) {
        return torneoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Torneo", id));
    }

    private void aplicar(Torneo t, TorneoRequest r) {
        Juego juego = juegoRepository.findById(r.juegoId())
                .orElseThrow(() -> new ResourceNotFoundException("Juego", r.juegoId()));
        t.setNombre(r.nombre().trim());
        t.setDescripcion(r.descripcion());
        t.setJuego(juego);
        t.setEstado(r.estado());
        t.setDestacado(Boolean.TRUE.equals(r.destacado()));
        t.setCuposMaximos(r.cuposMaximos());
        t.setFechaCierre(r.fechaCierre());

        t.getImagenes().clear();
        if (r.imagenes() != null) t.getImagenes().addAll(r.imagenes());
        t.getPremios().clear();
        if (r.premios() != null) t.getPremios().addAll(r.premios());
        t.getLlaves().clear();
        if (r.llaves() != null) t.getLlaves().addAll(r.llaves());
        t.getCalendario().clear();
        if (r.calendario() != null) {
            r.calendario().forEach(p -> t.getCalendario().add(new PartidoCalendario(p.ronda(), p.partido(), p.fecha())));
        }
    }

    /** Debe llamarse dentro de una transacción (las colecciones son LAZY). */
    static TorneoResponse aRespuesta(Torneo t) {
        return new TorneoResponse(
                t.getId(),
                t.getNombre(),
                t.getDescripcion(),
                t.getJuego().getId(),
                t.getJuego().getNombre(),
                t.getEstado(),
                t.isDestacado(),
                t.getCuposMaximos(),
                t.getCuposOcupados(),
                Math.max(0, t.getCuposMaximos() - t.getCuposOcupados()),
                t.getFechaCierre(),
                List.copyOf(t.getImagenes()),
                List.copyOf(t.getPremios()),
                t.getCalendario().stream().map(p -> new PartidoDto(p.getRonda(), p.getPartido(), p.getFecha())).toList(),
                List.copyOf(t.getLlaves()));
    }
}
