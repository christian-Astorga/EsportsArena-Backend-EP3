package com.esportsarena.api.service;

import com.esportsarena.api.dto.UsuarioRequest;
import com.esportsarena.api.dto.UsuarioResponse;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.exception.ResourceNotFoundException;
import com.esportsarena.api.model.Usuario;
import com.esportsarena.api.repository.EquipoRepository;
import com.esportsarena.api.repository.InscripcionRepository;
import com.esportsarena.api.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de usuarios (solo ADMINISTRADOR, restringido en el controlador). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EquipoRepository equipoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, InscripcionRepository inscripcionRepository,
                          EquipoRepository equipoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.equipoRepository = equipoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    static UsuarioResponse aRespuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.isActivo());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest r) {
        String correo = r.correo().trim().toLowerCase();
        if (r.password() == null || r.password().length() < 8) {
            throw new BusinessException("La contraseña es obligatoria y debe tener al menos 8 caracteres");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new BusinessException("Ya existe un usuario con el correo " + correo);
        }
        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(r.password()));
        u.setRol(r.rol());
        u.setActivo(r.activo() == null || r.activo());
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest r, String correoSolicitante) {
        Usuario u = buscar(id);
        String correo = r.correo().trim().toLowerCase();
        if (!u.getCorreo().equals(correo) && usuarioRepository.existsByCorreo(correo)) {
            throw new BusinessException("Ya existe un usuario con el correo " + correo);
        }
        boolean esSiMismo = u.getCorreo().equals(correoSolicitante);
        boolean pierdeAcceso = r.rol() != u.getRol() || Boolean.FALSE.equals(r.activo());
        if (esSiMismo && pierdeAcceso) {
            throw new BusinessException("No puedes cambiar tu propio rol ni desactivar tu propia cuenta");
        }
        u.setNombre(r.nombre().trim());
        u.setCorreo(correo);
        u.setRol(r.rol());
        if (r.activo() != null) u.setActivo(r.activo());
        if (r.password() != null && !r.password().isBlank()) {
            if (r.password().length() < 8) {
                throw new BusinessException("La contraseña debe tener al menos 8 caracteres");
            }
            u.setPassword(passwordEncoder.encode(r.password()));
        }
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public void eliminar(Long id, String correoSolicitante) {
        Usuario u = buscar(id);
        if (u.getCorreo().equals(correoSolicitante)) {
            throw new BusinessException("No puedes eliminar tu propia cuenta");
        }
        if (inscripcionRepository.existsByUsuarioId(id) || equipoRepository.existsByCapitanId(id)) {
            throw new BusinessException("El usuario tiene inscripciones o equipos asociados. Desactívalo en lugar de eliminarlo.");
        }
        usuarioRepository.delete(u);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
