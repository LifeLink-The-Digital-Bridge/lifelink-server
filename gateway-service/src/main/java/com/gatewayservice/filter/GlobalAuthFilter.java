package com.gatewayservice.filter;

import com.gatewayservice.dto.UserDTO;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;


import java.util.List;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/password-recovery",
            "/users/register"
    );

    public GlobalAuthFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = authHeader.substring(7);

        return webClientBuilder.build()
                .get()
                .uri("lb://AUTH-SERVICE/auth/validate?token=" + token)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .flatMap(user -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(builder -> builder
                                    .header("id", String.valueOf(user.getId()))
                                    .header("username", user.getUsername())
                                    .header("email", user.getEmail())
                                    .header("dob", String.valueOf(user.getDob()))
                                    .header("gender", String.valueOf(user.getGender()))
                                    .header("roles", String.join(",", user.getRoles()))
                            )
                            .build();
                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(error -> unauthorizedResponse(exchange, "Invalid token"));
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
