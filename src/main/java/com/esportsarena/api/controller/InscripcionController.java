package com.esportsarena.api.controller;

import com.esportsarena.api.dto.InscripcionRequest;
import com.esportsarena.api.dto.InscripcionResponse;
import com.esportsarena.api.security.AuthUtil;
import com.esportsarena.api.service.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inscripciones")
@Tag(name = "Inscripciones", description = "Inscripciones de usuarios a torneos (equivalente a las órdenes/boletas)")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
    @Operation(summary = "Inscribe al usuario autenticado en un torneo (valida estado, plazo y cupos)")
    public InscripcionResponse crear(@Valid @RequestBody InscripcionRequest request, Authentication authentication) {
        return inscripcionService.crear(authentication.getName(), request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @Operation(summary = "Lista todas las inscripciones (ADMINISTRADOR y VENDEDOR)")
    public List<InscripcionResponse> listar() {
        return inscripcionService.listarTodas();
    }

    @GetMapping("/mias")
    @Operation(summary = "Lista las inscripciones del usuario autenticado")
    public List<InscripcionResponse> mias(Authentication authentication) {
        return inscripcionService.listarMias(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una inscripción (personal interno o su dueño)")
    public InscripcionResponse obtener(@PathVariable Long id, Authentication authentication) {
        boolean personalInterno = AuthUtil.tieneRol(authentication, "ADMINISTRADOR")
                || AuthUtil.tieneRol(authentication, "VENDEDOR");
        return inscripcionService.obtener(id, authentication.getName(), personalInterno);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
    @Operation(summary = "Cancela una inscripción y libera el cupo (dueño o ADMINISTRADOR)")
    public void cancelar(@PathVariable Long id, Authentication authentication) {
        inscripcionService.cancelar(id, authentication.getName(), AuthUtil.tieneRol(authentication, "ADMINISTRADOR"));
    }
}
