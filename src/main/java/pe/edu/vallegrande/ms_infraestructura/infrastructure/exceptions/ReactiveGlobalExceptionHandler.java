package pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ReactiveGlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNotFoundException(
            NotFoundException ex, ServerWebExchange exchange) {
        log.error("Recurso no encontrado: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Recurso no encontrado",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBadRequestException(
            BadRequestException ex, ServerWebExchange exchange) {
        log.error("Solicitud incorrecta: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Solicitud incorrecta",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAccessDeniedException(
            AccessDeniedException ex, ServerWebExchange exchange) {
        log.error("Acceso denegado: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Acceso denegado",
            "No tienes permisos suficientes para acceder a este recurso",
            exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAuthenticationException(
            AuthenticationCredentialsNotFoundException ex, ServerWebExchange exchange) {
        log.error("Credenciales de autenticación no encontradas: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "No autenticado",
            "Se requiere autenticación para acceder a este recurso",
            exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        log.error("Error de validación: {}", ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Error de validación",
            "Los datos proporcionados no son válidos",
            exchange.getRequest().getPath().value()
        );
        errorResponse.put("fieldErrors", fieldErrors);
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Error interno del servidor: ", ex);
        
        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor",
            "Ha ocurrido un error inesperado. Por favor, contacte al administrador.",
            exchange.getRequest().getPath().value()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    private Map<String, Object> createErrorResponse(int status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}