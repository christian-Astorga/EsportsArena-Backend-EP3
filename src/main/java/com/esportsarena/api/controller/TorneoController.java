package com.esportsarena.api.controller;

import com.esportsarena.api.dto.TorneoRequest;
import com.esportsarena.api.dto.TorneoResponse;
import com.esportsarena.api.service.TorneoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/torneos")
@Tag(name = "Torneos", description = "CRUD de torneos (lectura pública, escritura solo ADMINISTRADOR)")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los torneos (público)")
    public List<TorneoResponse> listar() {
        return torneoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un torneo (público)")
    public TorneoResponse obtener(@PathVariable Long id) {
        return torneoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crea un torneo")
    public TorneoResponse crear(@Valid @RequestBody TorneoRequest request) {
        return torneoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualiza un torneo")
    public TorneoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TorneoRequest request) {
        return torneoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Elimina un torneo sin inscripciones")
    public void eliminar(@PathVariable Long id) {
        torneoService.eliminar(id);
    }
}
