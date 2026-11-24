package com.esportsarena.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.esportsarena.api.dto.InscripcionRequest;
import com.esportsarena.api.dto.InscripcionResponse;
import com.esportsarena.api.exception.BusinessException;
import com.esportsarena.api.exception.ResourceNotFoundException;
import com.esportsarena.api.model.*;
import com.esportsarena.api.repository.InscripcionRepository;
import com.esportsarena.api.repository.TorneoRepository;
import com.esportsarena.api.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/** Pruebas unitarias de la lógica de negocio: los repositorios se simulan con Mockito (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock InscripcionRepository inscripcionRepository;
    @Mock TorneoRepository torneoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks InscripcionService service;

    private Usuario usuario;
    private Torneo torneo;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Jugador");
        usuario.setCorreo("j@x.cl");
        usuario.setRol(Rol.CLIENTE);

        Juego juego = new Juego("Valorant", 5);
        juego.setId(2L);

        torneo = new Torneo();
        torneo.setId(10L);
        torneo.setNombre("Copa");
        torneo.setJuego(juego);
        torneo.setEstado(EstadoTorneo.INSCRIPCIONES_ABIERTAS);
        torneo.setCuposMaximos(16);
        torneo.setCuposOcupados(15);
        torneo.setFechaCierre(LocalDate.now().plusDays(5));
    }

    private void usuarioYTorneoExisten() {
        when(usuarioRepository.findByCorreo("j@x.cl")).thenReturn(Optional.of(usuario));
        when(torneoRepository.findById(10L)).thenReturn(Optional.of(torneo));
    }

    private InscripcionRequest solicitud() {
        return new InscripcionRequest(10L, "j@x.cl", 20);
    }

    @Test
    void creaInscripcionYOcupaUnCupo() {
        usuarioYTorneoExisten();
        when(inscripcionRepository.existsByUsuarioIdAndTorneoId(1L, 10L)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> {
            Inscripcion i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        InscripcionResponse r = service.crear("j@x.cl", solicitud());

        assertEquals(100L, r.id());
        assertEquals("Copa", r.torneoNombre());
        assertEquals(16, torneo.getCuposOcupados());
        verify(torneoRepository).save(torneo);
    }

    @Test
    void rechazaSiElTorneoNoTieneCupos() {
        torneo.setCuposOcupados(16);
        usuarioYTorneoExisten();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crear("j@x.cl", solicitud()));

        assertTrue(ex.getMessage().contains("no tiene cupos"));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void rechazaSiLasInscripcionesNoEstanAbiertas() {
        torneo.setEstado(EstadoTorneo.EN_CURSO);
        usuarioYTorneoExisten();

        assertThrows(BusinessException.class, () -> service.crear("j@x.cl", solicitud()));
        assertEquals(15, torneo.getCuposOcupados());
    }

    @Test
    void rechazaSiElPlazoDeInscripcionVencio() {
        torneo.setFechaCierre(LocalDate.now().minusDays(1));
        usuarioYTorneoExisten();

        BusinessException ex = assertThrows(BusinessException.class, () -> service.crear("j@x.cl", solicitud()));

        assertTrue(ex.getMessage().contains("venció"));
    }

    @Test
    void rechazaInscripcionDuplicada() {
        usuarioYTorneoExisten();
        when(inscripcionRepository.existsByUsuarioIdAndTorneoId(1L, 10L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.crear("j@x.cl", solicitud()));
        assertEquals(15, torneo.getCuposOcupados());
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    void falla404SiElTorneoNoExiste() {
        when(usuarioRepository.findByCorreo("j@x.cl")).thenReturn(Optional.of(usuario));
        when(torneoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.crear("j@x.cl", solicitud()));
    }

    @Test
    void cancelarLiberaElCupo() {
        torneo.setCuposOcupados(16);
        Inscripcion i = new Inscripcion();
        i.setId(100L);
        i.setUsuario(usuario);
        i.setTorneo(torneo);
        when(inscripcionRepository.findById(100L)).thenReturn(Optional.of(i));

        service.cancelar(100L, "j@x.cl", false);

        assertEquals(15, torneo.getCuposOcupados());
        verify(inscripcionRepository).delete(i);
    }

    @Test
    void unClienteNoPuedeCancelarLaInscripcionDeOtro() {
        Inscripcion i = new Inscripcion();
        i.setId(100L);
        i.setUsuario(usuario);
        i.setTorneo(torneo);
        when(inscripcionRepository.findById(100L)).thenReturn(Optional.of(i));

        assertThrows(AccessDeniedException.class, () -> service.cancelar(100L, "otro@x.cl", false));
        verify(inscripcionRepository, never()).delete(any());
    }
}
