package com.esportsarena.api.exception;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce excepciones a respuestas JSON uniformes.
 * No hay un handler genérico para Exception a propósito: así AccessDeniedException
 * llega al RestAccessDeniedHandler de Spring Security y responde 403 (no 500).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> noEncontrado(ResourceNotFoundException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> reglaDeNegocio(BusinessException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> cuerpoInvalido(Exception ex) {
        return construir(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato inválido", List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> autenticacion(AuthenticationException ex) {
        String mensaje = ex instanceof DisabledException ? "El usuario está inactivo"
                : ex instanceof BadCredentialsException ? ex.getMessage() : "No autenticado";
        return construir(HttpStatus.UNAUTHORIZED, mensaje, List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> concurrencia(ObjectOptimisticLockingFailureException ex) {
        return construir(HttpStatus.CONFLICT, "El recurso fue modificado por otra operación. Intenta nuevamente.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT, "La operación viola una restricción de datos (duplicado o referencia existente).", List.of());
    }

    private ResponseEntity<ApiError> construir(HttpStatus status, String mensaje, List<String> detalles) {
        ApiError cuerpo = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, detalles);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
