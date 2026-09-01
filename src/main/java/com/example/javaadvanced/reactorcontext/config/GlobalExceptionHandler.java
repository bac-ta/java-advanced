package com.example.javaadvanced.reactorcontext.config;

import com.example.javaadvanced.reactorcontext.context.RequestContextKeys;
import com.example.javaadvanced.reactorcontext.order.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNotFound(OrderNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Have an occur error");
    }

    private Mono<ResponseEntity<Map<String, Object>>> errorResponse(HttpStatus status, String message) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(RequestContextKeys.TRACE_ID, "N/A");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", Instant.now().toString());
            body.put("status", status.value());
            body.put("error", status.getReasonPhrase());
            body.put("message", message);
            body.put("traceId", traceId);
            return Mono.just(ResponseEntity.status(status).body(body));
        });
    }
}
