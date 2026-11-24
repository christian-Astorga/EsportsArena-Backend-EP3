package com.esportsarena.api.service;

import com.esportsarena.api.dto.InscripcionRequest;
import com.esportsarena.api.dto.InscripcionResponse;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.exception.ResourceNotFoundException;
import com.esportsarena.api.model.EstadoTorneo;
import com.esportsarena.api.model.Inscripcion;
import com.esportsarena.api.model.Torneo;
import com.esportsarena.api.model.Usuario;
import com.esportsarena.api.repository.InscripcionRepository;
import com.esportsarena.api.repository.TorneoRepository;
import com.esportsarena.api.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio principal: una inscripción solo es válida si el torneo está abierto,
 * no ha pasado su fecha de cierre, tiene cupos y el usuario no estaba ya inscrito.
 */
@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final TorneoRepository torneoRepository;
    private final UsuarioRepository usuarioRepository;

    public InscripcionService(InscripcionRepository inscripcionRepository, TorneoRepository torneoRepository,
                              UsuarioRepository usuarioRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.torneoRepository = torneoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public InscripcionResponse crear(String correoUsuario, InscripcionRequest r) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + correoUsuario + " no encontrado"));
        Torneo torneo = torneoRepository.findById(r.torneoId())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", r.torneoId()));

        if (torneo.getEstado() != EstadoTorneo.INSCRIPCIONES_ABIERTAS) {
            throw new BusinessException("Las inscripciones de \"" + torneo.getNombre() + "\" no están abiertas ("
                    + torneo.getEstado().getEtiqueta() + ")");
        }
        if (torneo.getFechaCierre() != null && torneo.getFechaCierre().isBefore(LocalDate.now())) {
            throw new BusinessException("El plazo de inscripción de \"" + torneo.getNombre() + "\" ya venció");
        }
        if (torneo.getCuposOcupados() >= torneo.getCuposMaximos()) {
            throw new BusinessException("El torneo \"" + torneo.getNombre() + "\" no tiene cupos disponibles");
        }
        if (inscripcionRepository.existsByUsuarioIdAndTorneoId(usuario.getId(), torneo.getId())) {
            throw new BusinessException("Ya estás inscrito en el torneo \"" + torneo.getNombre() + "\"");
        }

        torneo.setCuposOcupados(torneo.getCuposOcupados() + 1);
        torneoRepository.save(torneo);

        Inscripcion i = new Inscripcion();
        i.setUsuario(usuario);
        i.setTorneo(torneo);
        i.setCorreoContacto(r.correo().trim());
        i.setEdad(r.edad());
        return aRespuesta(inscripcionRepository.save(i));
    }

    /** ADMINISTRADOR y VENDEDOR: todas las inscripciones. */
    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarTodas() {
        return inscripcionRepository.findAll().stream().map(InscripcionService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarMias(String correoUsuario) {
        return inscripcionRepository.findByUsuarioCorreoOrderByFechaDesc(correoUsuario).stream()
                .map(InscripcionService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public InscripcionResponse obtener(Long id, String correoSolicitante, boolean esPersonalInterno) {
        Inscripcion i = buscar(id);
        verificarAcceso(i, correoSolicitante, esPersonalInterno);
        return aRespuesta(i);
    }

    /** Cancela una inscripción y libera el cupo. La puede cancelar su dueño o un ADMINISTRADOR. */
    @Transactional
    public void cancelar(Long id, String correoSolicitante, boolean esAdministrador) {
        Inscripcion i = buscar(id);
        verificarAcceso(i, correoSolicitante, esAdministrador);
        Torneo torneo = i.getTorneo();
        torneo.setCuposOcupados(Math.max(0, torneo.getCuposOcupados() - 1));
        torneoRepository.save(torneo);
        inscripcionRepository.delete(i);
    }

    private void verificarAcceso(Inscripcion i, String correoSolicitante, boolean tienePermisoTotal) {
        if (!tienePermisoTotal && !i.getUsuario().getCorreo().equals(correoSolicitante)) {
            throw new AccessDeniedException("No puedes acceder a inscripciones de otros usuarios");
        }
    }

    private Inscripcion buscar(Long id) {
        return inscripcionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inscripción", id));
    }

    static InscripcionResponse aRespuesta(Inscripcion i) {
        return new InscripcionResponse(
                i.getId(),
                i.getTorneo().getId(),
                i.getTorneo().getNombre(),
                i.getUsuario().getId(),
                i.getUsuario().getNombre(),
                i.getCorreoContacto(),
                i.getEdad(),
                i.getFecha());
    }
}
