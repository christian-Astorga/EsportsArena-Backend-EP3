package com.esportsarena.api.exception;

/** Regla de negocio incumplida (cupos agotados, correo duplicado, etc.). Se responde con HTTP 409. */
public class BusinessException extends RuntimeException {
    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
