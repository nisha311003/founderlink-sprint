package com.founderlink.apiGateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status;
        String error;
        String message;

        if (ex instanceof NotFoundException notFoundException) {
            String exceptionMessage = notFoundException.getMessage();

            // Check if it's a service not found error (service unavailable)
            if (exceptionMessage != null && exceptionMessage.contains("Unable to find instance")) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                error = "Service Unavailable";
                message = "The requested microservice is currently unavailable. Please try again later.";
                logError(status, error, message, exceptionMessage);
            } else {
                // Generic 404 for other not found cases
                status = HttpStatus.NOT_FOUND;
                error = "Not Found";
                message = exceptionMessage != null ? exceptionMessage : "The requested resource was not found";
                logError(status, error, message, exceptionMessage);
            }
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            error = "Bad Request";
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters";
            logError(status, error, message, null);
        } else {
            // Handle all other exceptions as Internal Server Error
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Internal Server Error";
            message = "An unexpected error occurred. Please contact support if the problem persists.";
            logError(status, error, ex.getMessage(), ex.getClass().getName());
        }

        exchange.getResponse().setStatusCode(status);

        Map<String, Object> errorResponse = buildErrorResponse(error, message, status, exchange.getRequest().getPath().value());
        String jsonResponse;
        try {
            jsonResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            jsonResponse = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\",\"status\":%d}",
                error,
                message.replace("\"", "\\\""),
                LocalDateTime.now().format(dateFormatter),
                status.value()
            );
        }

        final String finalJsonResponse = jsonResponse;
        return exchange.getResponse()
            .writeWith(Mono.fromCallable(() ->
                bufferFactory.wrap(finalJsonResponse.getBytes(StandardCharsets.UTF_8))
            ));
    }

    private Map<String, Object> buildErrorResponse(String error, String message, HttpStatus status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("status", status.value());
        response.put("timestamp", LocalDateTime.now().format(dateFormatter));
        response.put("path", path);
        return response;
    }

    private void logError(HttpStatus status, String error, String message, String details) {
        String logMessage = String.format(
            "[GATEWAY ERROR] Status: %d %s | Error: %s | Message: %s",
            status.value(),
            status.getReasonPhrase(),
            error,
            message
        );
        if (details != null) {
            System.out.println(logMessage + " | Details: " + details);
        } else {
            System.out.println(logMessage);
        }
    }
}

