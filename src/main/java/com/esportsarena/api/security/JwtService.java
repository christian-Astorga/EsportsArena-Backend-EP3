package com.esportsarena.api.security;

import com.esportsarena.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Genera y valida tokens JWT firmados con HMAC-SHA.
 * - access token: corta duración (se envía en cada petición en el header Authorization).
 * - refresh token: larga duración, solo sirve en /auth/refresh para obtener un nuevo par de tokens.
 * El claim "tipo" evita que un refresh token se use como access token.
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(@Value("${app.jwt.secret}") String secretBase64,
                      @Value("${app.jwt.access-minutes}") long accessMinutos,
                      @Value("${app.jwt.refresh-days}") long refreshDias) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.accessMs = accessMinutos * 60_000L;
        this.refreshMs = refreshDias * 24L * 60L * 60_000L;
    }

    public String generarAccessToken(Usuario usuario) {
        return construir(usuario, TIPO_ACCESS, accessMs);
    }

    public String generarRefreshToken(Usuario usuario) {
        return construir(usuario, TIPO_REFRESH, refreshMs);
    }

    public long getAccessSegundos() {
        return accessMs / 1000;
    }

    /** Verifica firma y expiración. Lanza JwtException (o subclases) si el token no es válido. */
    public Claims validar(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    private String construir(Usuario usuario, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }
}
