package com.esportsarena.api.config;

import com.esportsarena.api.model.*;
import com.esportsarena.api.repository.JuegoRepository;
import com.esportsarena.api.repository.TorneoRepository;
import com.esportsarena.api.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final JuegoRepository juegoRepository;
    private final TorneoRepository torneoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, JuegoRepository juegoRepository,
                           TorneoRepository torneoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.juegoRepository = juegoRepository;
        this.torneoRepository = torneoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            crearUsuario("Administrador", "admin@esports.cl", "Admin1234!", Rol.ADMINISTRADOR);
            crearUsuario("Vendedor Demo", "vendedor@esports.cl", "Vendedor1234!", Rol.VENDEDOR);
            crearUsuario("Jugador Demo", "cliente@esports.cl", "Cliente1234!", Rol.CLIENTE);
            log.info("Usuarios demo creados (admin@esports.cl, vendedor@esports.cl, cliente@esports.cl)");
        }
        if (juegoRepository.count() == 0) {
            Juego lol = juegoRepository.save(new Juego("League of Legends", 5));
            Juego val = juegoRepository.save(new Juego("Valorant", 5));
            Juego cs2 = juegoRepository.save(new Juego("Counter-Strike 2", 5));
            Juego rl = juegoRepository.save(new Juego("Rocket League", 3));

            LocalDate hoy = LocalDate.now();
            crearTorneo("Copa de Verano League of Legends", lol, EstadoTorneo.INSCRIPCIONES_ABIERTAS, true, 16, 12,
                    hoy.plusDays(30), "El torneo estival más grande de LoL de la región con premios en efectivo.",
                    List.of("1er Lugar: $1,500 USD", "2do Lugar: $500 USD"),
                    List.of("Llave Superior 8 Equipos", "Llave de Consolación"),
                    new PartidoCalendario("Cuartos de Final", "Equipo A vs Equipo B", hoy.plusDays(35)), 1);
            crearTorneo("Valorant Challenger Series", val, EstadoTorneo.EN_CURSO, true, 32, 32,
                    hoy.minusDays(20), "Compite al más alto nivel en la liga Challenger con casters en vivo.",
                    List.of("1er Lugar: $2,000 USD + Cupo Clasificatorio"),
                    List.of("Eliminación Directa al mejor de 3"),
                    new PartidoCalendario("Semifinales", "KRÜ vs Leviatán", hoy.plusDays(3)), 3);
            crearTorneo("CS2 Masters Championship", cs2, EstadoTorneo.INSCRIPCIONES_ABIERTAS, false, 16, 8,
                    hoy.plusDays(45), "Demuestra la puntería y estrategia táctica de tu escuadra en CS2.",
                    List.of("1er Lugar: $1,000 USD"),
                    List.of("Formato GSL + Playoffs"),
                    new PartidoCalendario("Fase de Grupos", "Grupo A", hoy.plusDays(50)), 5);
            crearTorneo("Rocket League 3v3 Open", rl, EstadoTorneo.FINALIZADO, false, 8, 8,
                    hoy.minusDays(90), "Torneo relámpago de Rocket League modalidad 3v3.",
                    List.of("1er Lugar: $300 USD"),
                    List.of("Doble Eliminación"),
                    new PartidoCalendario("Gran Final", "Nitro FC vs Speed Kings", hoy.minusDays(85)), 7);
            log.info("Juegos y torneos demo creados");
        }
    }

    private void crearUsuario(String nombre, String correo, String clave, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(clave));
        u.setRol(rol);
        usuarioRepository.save(u);
    }

    private void crearTorneo(String nombre, Juego juego, EstadoTorneo estado, boolean destacado, int maximos,
                             int ocupados, LocalDate cierre, String descripcion, List<String> premios,
                             List<String> llaves, PartidoCalendario partido, int semillaImagen) {
        Torneo t = new Torneo();
        t.setNombre(nombre);
        t.setJuego(juego);
        t.setEstado(estado);
        t.setDestacado(destacado);
        t.setCuposMaximos(maximos);
        t.setCuposOcupados(ocupados);
        t.setFechaCierre(cierre);
        t.setDescripcion(descripcion);
        t.getImagenes().add("https://picsum.photos/400/250?random=" + semillaImagen);
        t.getImagenes().add("https://picsum.photos/400/250?random=" + (semillaImagen + 1));
        t.getPremios().addAll(premios);
        t.getLlaves().addAll(llaves);
        t.getCalendario().add(partido);
        torneoRepository.save(t);
    }
}
