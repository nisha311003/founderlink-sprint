package com.founderlink.apiGateway.filter;

import com.founderlink.apiGateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("🔥 JWT FILTER EXECUTED");
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getPath().toString();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // get Authorization header
        String authHeader = request.getHeaders()
                .getFirst("Authorization");

        System.out.println("Auth Header: "+authHeader);

        // no token — reject
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rejectRequest(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header"
            );
        }

        String token = authHeader.substring(7);
        System.out.println("Token: "+token);

        System.out.println("Is valid: "+jwtUtil.validateToken(token));
        // invalid token — reject
        if (!jwtUtil.validateToken(token)) {
            return rejectRequest(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }

        // token is valid — extract user info
        String email  = jwtUtil.extractEmail(token);
        String role   = jwtUtil.extractRole(token);
        Long userId   = jwtUtil.extractUserId(token);

        System.out.println("🎫 JWT EXTRACTED - Email: " + email + ", Role: " + role + ", UserId: " + userId);

        //inject user info as headers for downstream services
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-User-Email", email)
                        .header("X-User-Role",  role)
                        .header("X-User-Id",
                                userId != null ? userId.toString() : "")
                )
                .build();

        return chain.filter(modifiedExchange);

    }
    private Mono<Void> rejectRequest(ServerWebExchange exchange,
                                     HttpStatus status,
                                     String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders()
                .add("Content-Type", "application/json");

        byte[] bytes = ("{\"error\":\"" + status.getReasonPhrase() +
                "\",\"message\":\"" + message + "\"}")
                .getBytes();

        var buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
    private boolean isPublicPath(String path) {
        return path.startsWith("/founderlink/auth/register") ||
                path.startsWith("/founderlink/auth/login") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/founderlink-docs/") ||
                path.startsWith("/webjars/");
    }

}
