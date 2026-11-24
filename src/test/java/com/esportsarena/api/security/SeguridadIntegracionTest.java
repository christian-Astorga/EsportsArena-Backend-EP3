package com.esportsarena.api.security;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integración: levantan la aplicación completa con H2 y verifican
 * autenticación (401), autorización por rol (403) y el flujo JWT de punta a punta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SeguridadIntegracionTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private JsonNode login(String correo, String password) throws Exception {
        String json = mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("correo", correo, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json);
    }

    private String accessToken(String correo, String password) throws Exception {
        return login(correo, password).get("accessToken").asText();
    }

    @Test
    void catalogoDeTorneosEsPublico() throws Exception {
        mvc.perform(get("/api/v1/torneos")).andExpect(status().isOk());
    }

    @Test
    void usuariosSinTokenResponde401() throws Exception {
        mvc.perform(get("/api/v1/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginConClaveIncorrectaResponde401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("correo", "admin@esports.cl", "password", "mala-clave"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeListarUsuarios() throws Exception {
        String token = accessToken("cliente@esports.cl", "Cliente1234!");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPuedeListarUsuarios() throws Exception {
        String token = accessToken("admin@esports.cl", "Admin1234!");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorVeInscripcionesPeroNoPuedeEliminarTorneos() throws Exception {
        String token = accessToken("vendedor@esports.cl", "Vendedor1234!");
        mvc.perform(get("/api/v1/inscripciones").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/v1/torneos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshTokenEntregaUnNuevoParDeTokens() throws Exception {
        String refresh = login("cliente@esports.cl", "Cliente1234!").get("refreshToken").asText();
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void refreshTokenNoSirveComoAccessToken() throws Exception {
        String refresh = login("cliente@esports.cl", "Cliente1234!").get("refreshToken").asText();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteSeInscribeUnaVezYElSegundoIntentoDa409() throws Exception {
        String token = accessToken("cliente@esports.cl", "Cliente1234!");
        String cuerpo = mapper.writeValueAsString(Map.of("torneoId", 3, "correo", "cliente@esports.cl", "edad", 22));

        mvc.perform(post("/api/v1/inscripciones").header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/inscripciones").header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void inscripcionConEdadInvalidaDa400() throws Exception {
        String token = accessToken("cliente@esports.cl", "Cliente1234!");
        String cuerpo = mapper.writeValueAsString(Map.of("torneoId", 1, "correo", "cliente@esports.cl", "edad", 10));

        mvc.perform(post("/api/v1/inscripciones").header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }
}
