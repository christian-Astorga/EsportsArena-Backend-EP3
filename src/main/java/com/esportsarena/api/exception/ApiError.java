package com.esportsarena.api.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(LocalDateTime timestamp, int status, String error, String mensaje, List<String> detalles) {
    public static ApiError de(int status, String error, String mensaje) {
        return new ApiError(LocalDateTime.now(), status, error, mensaje, List.of());
    }
}
