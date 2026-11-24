package com.esportsarena.api.dto;

import com.esportsarena.api.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** En la creación la contraseña es obligatoria; en la actualización es opcional (si viene vacía no se cambia). */
public record UsuarioRequest(
        @NotBlank @Size(min = 3, max = 100) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @Size(max = 72) String password,
        @NotNull Rol rol,
        Boolean activo) {}
