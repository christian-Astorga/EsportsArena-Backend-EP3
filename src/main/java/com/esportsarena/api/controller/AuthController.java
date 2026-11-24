package com.esportsarena.api.controller;

import com.esportsarena.api.dto.AuthResponse;
import com.esportsarena.api.dto.LoginRequest;
import com.esportsarena.api.dto.RefreshRequest;
import com.esportsarena.api.dto.RegistroRequest;
import com.esportsarena.api.dto.UsuarioResponse;
import com.esportsarena.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, login, renovación de token y perfil")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un nuevo usuario con rol CLIENTE y devuelve sus tokens")
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve accessToken + refreshToken (JWT)")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Canjea un refreshToken válido por un nuevo par de tokens")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.renovar(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve el usuario autenticado (útil para restaurar la sesión al recargar el frontend)")
    public UsuarioResponse me(Authentication authentication) {
        return authService.perfil(authentication.getName());
    }
}
