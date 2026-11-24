package com.esportsarena.api.dto;

import com.esportsarena.api.model.Rol;

public record UsuarioResponse(Long id, String nombre, String correo, Rol rol, boolean activo) {}
