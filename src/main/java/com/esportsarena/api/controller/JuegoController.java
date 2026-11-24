package com.esportsarena.api.controller;

import com.esportsarena.api.dto.JuegoDto;
import com.esportsarena.api.service.JuegoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/juegos")
@Tag(name = "Juegos", description = "Catálogo de videojuegos (lectura pública, escritura solo ADMINISTRADOR)")
public class JuegoController {

    private final JuegoService juegoService;

    public JuegoController(JuegoService juegoService) {
        this.juegoService = juegoService;
    }

    @GetMapping
    @Operation(summary = "Lista los juegos (público)")
    public List<JuegoDto> listar() {
        return juegoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un juego por id (público)")
    public JuegoDto obtener(@PathVariable Long id) {
        return juegoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crea un juego")
    public JuegoDto crear(@Valid @RequestBody JuegoDto dto) {
        return juegoService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualiza un juego")
    public JuegoDto actualizar(@PathVariable Long id, @Valid @RequestBody JuegoDto dto) {
        return juegoService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Elimina un juego sin torneos ni equipos asociados")
    public void eliminar(@PathVariable Long id) {
        juegoService.eliminar(id);
    }
}
