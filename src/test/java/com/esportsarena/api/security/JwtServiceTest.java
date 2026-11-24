package com.esportsarena.api.security;

import static org.junit.jupiter.api.Assertions.*;

import com.esportsarena.api.model.Rol;
import com.esportsarena.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static String secreto(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static final String SECRETO = secreto("clave-de-prueba-con-mas-de-32-bytes-0123456789");

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setCorreo("admin@esports.cl");
        u.setRol(Rol.ADMINISTRADOR);
        return u;
    }

    @Test
    void accessTokenContieneUsuarioRolYTipo() {
        JwtService jwt = new JwtService(SECRETO, 15, 7);

        Claims claims = jwt.validar(jwt.generarAccessToken(usuario()));

        assertEquals("admin@esports.cl", claims.getSubject());
        assertEquals("ADMINISTRADOR", claims.get("rol", String.class));
        assertEquals(JwtService.TIPO_ACCESS, claims.get("tipo", String.class));
    }

    @Test
    void refreshTokenTieneTipoRefresh() {
        JwtService jwt = new JwtService(SECRETO, 15, 7);

        Claims claims = jwt.validar(jwt.generarRefreshToken(usuario()));

        assertEquals(JwtService.TIPO_REFRESH, claims.get("tipo", String.class));
    }

    @Test
    void tokenExpiradoEsRechazado() {
        JwtService jwt = new JwtService(SECRETO, -1, 7); // expira un minuto en el pasado
        String token = jwt.generarAccessToken(usuario());

        assertThrows(JwtException.class, () -> jwt.validar(token));
    }

    @Test
    void tokenFirmadoConOtraClaveEsRechazado() {
        JwtService emisor = new JwtService(secreto("otra-clave-distinta-para-el-emisor-0123456789"), 15, 7);
        JwtService validador = new JwtService(SECRETO, 15, 7);

        assertThrows(JwtException.class, () -> validador.validar(emisor.generarAccessToken(usuario())));
    }

    @Test
    void tokenAlteradoEsRechazado() {
        JwtService jwt = new JwtService(SECRETO, 15, 7);
        String token = jwt.generarAccessToken(usuario());
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThrows(JwtException.class, () -> jwt.validar(alterado));
    }
}
