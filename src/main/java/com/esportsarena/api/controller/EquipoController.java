package com.esportsarena.api.controller;

import com.esportsarena.api.dto.EquipoRequest;
import com.esportsarena.api.dto.EquipoResponse;
import com.esportsarena.api.service.EquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipos")
@Tag(name = "Equipos", description = "Registro de equipos (el PIN se guarda con hash y nunca se devuelve)")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
    @Operation(summary = "Registra un equipo; el usuario autenticado queda como capitán")
    public EquipoResponse crear(@Valid @RequestBody EquipoRequest request, Authentication authentication) {
        return equipoService.crear(authentication.getName(), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Lista todos los equipos (solo ADMINISTRADOR)")
    public List<EquipoResponse> listar() {
        return equipoService.listarTodos();
    }

    @GetMapping("/mios")
    @Operation(summary = "Lista los equipos del usuario autenticado")
    public List<EquipoResponse> mios(Authentication authentication) {
        return equipoService.listarMios(authentication.getName());
    }
}
