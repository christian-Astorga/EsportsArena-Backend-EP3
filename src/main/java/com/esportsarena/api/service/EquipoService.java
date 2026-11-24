package com.esportsarena.api.service;

import com.esportsarena.api.dto.EquipoRequest;
import com.esportsarena.api.dto.EquipoResponse;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.exception.ResourceNotFoundException;
import com.esportsarena.api.model.Equipo;
import com.esportsarena.api.model.Juego;
import com.esportsarena.api.model.Usuario;
import com.esportsarena.api.repository.EquipoRepository;
import com.esportsarena.api.repository.JuegoRepository;
import com.esportsarena.api.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final JuegoRepository juegoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public EquipoService(EquipoRepository equipoRepository, JuegoRepository juegoRepository,
                         UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.equipoRepository = equipoRepository;
        this.juegoRepository = juegoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EquipoResponse crear(String correoCapitan, EquipoRequest r) {
        Usuario capitan = usuarioRepository.findByCorreo(correoCapitan)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + correoCapitan + " no encontrado"));
        Juego juego = juegoRepository.findById(r.juegoId())
                .orElseThrow(() -> new ResourceNotFoundException("Juego", r.juegoId()));

        if (r.integrantes() < juego.getMinIntegrantes()) {
            throw new BusinessException("Para " + juego.getNombre() + " se exige un mínimo de "
                    + juego.getMinIntegrantes() + " integrantes");
        }

        Equipo e = new Equipo();
        e.setNombre(r.nombre().trim());
        e.setJuego(juego);
        e.setIntegrantes(r.integrantes());
        e.setPinHash(passwordEncoder.encode(r.pin()));
        e.setCapitan(capitan);
        return aRespuesta(equipoRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listarTodos() {
        return equipoRepository.findAll().stream().map(EquipoService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listarMios(String correoCapitan) {
        return equipoRepository.findByCapitanCorreo(correoCapitan).stream().map(EquipoService::aRespuesta).toList();
    }

    static EquipoResponse aRespuesta(Equipo e) {
        return new EquipoResponse(
                e.getId(),
                e.getJuego().getId(),
                e.getJuego().getNombre(),
                e.getNombre(),
                e.getIntegrantes(),
                e.getCapitan().getId(),
                e.getCapitan().getNombre());
    }
}
