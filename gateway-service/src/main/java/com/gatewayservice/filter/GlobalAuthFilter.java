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
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;


import java.util.List;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "^/auth/login(/.*)?$",
            "^/auth/password-recovery(/.*)?$",
            "^/users/register$"
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

        System.out.println("Requested Path: " + path);

        System.out.println("Auth header: " + authHeader);
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
                                    .header("user", String.valueOf(user))
                                    .header("email", user.getEmail())
                                    .header("roles", String.valueOf(user.getRoles()))
                            )
                            .build();
                    System.out.println("Passing to service with headers: ");
                    mutatedExchange.getRequest().getHeaders().forEach((key, value) -> System.out.println(key + ": " + value));

                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(error -> unauthorizedResponse(exchange, "Invalid token"));
    }

    private boolean isPublicEndpoint(String path) {
        System.out.println("Path :"+path+" = "+PUBLIC_ENDPOINTS.stream().anyMatch(path::matches));
        return PUBLIC_ENDPOINTS.stream().anyMatch(publicPath -> path.matches(publicPath));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        String body = "{\"message\":\"" + message + "\"}";
        DataBuffer buffer = bufferFactory.wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
