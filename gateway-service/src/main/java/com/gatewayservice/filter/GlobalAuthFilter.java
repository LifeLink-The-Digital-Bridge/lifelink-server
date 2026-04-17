package com.gatewayservice.filter;

import com.gatewayservice.dto.UserDTO;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_VALIDATE_PATH = "/auth/validate";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    private static final String USER_ID_HEADER = "id";
    private static final String USERNAME_HEADER = "username";
    private static final String EMAIL_HEADER = "email";
    private static final String DOB_HEADER = "dob";
    private static final String GENDER_HEADER = "gender";
    private static final String ROLES_HEADER = "roles";

    private final WebClient authWebClient;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/password-recovery",
            "/users/register",
            "/api/health/records/documents/"
    );

    public GlobalAuthFilter(WebClient.Builder webClientBuilder) {
        this.authWebClient = webClientBuilder.clone().baseUrl("lb://AUTH-SERVICE").build();
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

        return authWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(AUTH_VALIDATE_PATH)
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(UserDTO.class)
                .flatMap(user -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(builder -> builder
                                    .header(USER_ID_HEADER, headerValue(user.getId()))
                                    .header(USERNAME_HEADER, headerValue(user.getUsername()))
                                    .header(EMAIL_HEADER, headerValue(user.getEmail()))
                                    .header(DOB_HEADER, headerValue(user.getDob()))
                                    .header(GENDER_HEADER, headerValue(user.getGender()))
                                    .header(ROLES_HEADER, joinRoles(user.getRoles()))
                            )
                            .build();
                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(error -> unauthorizedResponse(exchange, INVALID_TOKEN_MESSAGE));
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

    private String joinRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return String.join(",", roles);
    }

    private String headerValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
