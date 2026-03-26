package com.founderlink.apiGateway.exception;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testServiceNotFoundException() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/founderlink/startups")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        Throwable exception = new NotFoundException("503 SERVICE_UNAVAILABLE \"Unable to find instance for STARTUPSERVICE\"");

        // When
        Mono<Void> result = exceptionHandler.handle(exchange, exception);

        // Then
        result.block();
        
        assertThat(exchange.getResponse().getStatusCode()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(503);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void testGenericNotFoundException() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/founderlink/users/999")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        Throwable exception = new NotFoundException("Resource not found");

        // When
        Mono<Void> result = exceptionHandler.handle(exchange, exception);

        // Then
        result.block();
        
        assertThat(exchange.getResponse().getStatusCode()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(404);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void testIllegalArgumentException() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/founderlink/auth")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        Throwable exception = new IllegalArgumentException("Invalid request parameters");

        // When
        Mono<Void> result = exceptionHandler.handle(exchange, exception);

        // Then
        result.block();
        
        assertThat(exchange.getResponse().getStatusCode()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void testGeneralException() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .post("/founderlink/users")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        Throwable exception = new RuntimeException("Unexpected error");

        // When
        Mono<Void> result = exceptionHandler.handle(exchange, exception);

        // Then
        result.block();
        
        assertThat(exchange.getResponse().getStatusCode()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(500);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }
}

