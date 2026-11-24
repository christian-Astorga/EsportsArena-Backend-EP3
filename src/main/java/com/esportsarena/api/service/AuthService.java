package com.esportsarena.api.service;

import com.esportsarena.api.dto.AuthResponse;
import com.esportsarena.api.dto.LoginRequest;
import com.esportsarena.api.dto.RefreshRequest;
import com.esportsarena.api.dto.RegistroRequest;
import com.esportsarena.api.dto.UsuarioResponse;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.model.Rol;
import com.esportsarena.api.model.Usuario;
import com.esportsarena.api.repository.UsuarioRepository;
import com.esportsarena.api.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Registro público: siempre crea un usuario con rol CLIENTE (el rol no lo elige quien se registra). */
    @Transactional
    public AuthResponse registrar(RegistroRequest r) {
        String correo = r.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new BusinessException("Ya existe un usuario con el correo " + correo);
        }
        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(r.password()));
        u.setRol(Rol.CLIENTE);
        return emitirTokens(usuarioRepository.save(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest r) {
        String correo = r.correo().trim().toLowerCase();
        // Lanza BadCredentialsException / DisabledException si las credenciales no sirven (→ 401).
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, r.password()));
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        return emitirTokens(u);
    }

    /** Renovación: canjea un refresh token válido por un nuevo par de tokens. */
    @Transactional(readOnly = true)
    public AuthResponse renovar(RefreshRequest r) {
        try {
            Claims claims = jwtService.validar(r.refreshToken());
            if (!JwtService.TIPO_REFRESH.equals(claims.get("tipo", String.class))) {
                throw new BadCredentialsException("El token entregado no es un refresh token");
            }
            Usuario u = usuarioRepository.findByCorreo(claims.getSubject())
                    .filter(Usuario::isActivo)
                    .orElseThrow(() -> new BadCredentialsException("Usuario no disponible"));
            return emitirTokens(u);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        return UsuarioService.aRespuesta(u);
    }

    private AuthResponse emitirTokens(Usuario u) {
        return new AuthResponse(
                jwtService.generarAccessToken(u),
                jwtService.generarRefreshToken(u),
                "Bearer",
                jwtService.getAccessSegundos(),
                UsuarioService.aRespuesta(u));
    }
}
