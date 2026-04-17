package com.gatewayservice.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalAuthFilterTest {

    private final GlobalAuthFilter filter = new GlobalAuthFilter(WebClient.builder());

    @Test
    void allowsPublicEndpointWithoutAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/auth/login").build()
        );

        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        GatewayFilterChain chain = chainInvokedChain(chainInvoked);

        filter.filter(exchange, chain).block();

        assertTrue(chainInvoked.get());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsProtectedEndpointWithoutAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/profile").build()
        );

        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        GatewayFilterChain chain = chainInvokedChain(chainInvoked);

        filter.filter(exchange, chain).block();

        assertFalse(chainInvoked.get());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsProtectedEndpointWhenAuthorizationIsNotBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Basic some-token")
                        .build()
        );

        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        GatewayFilterChain chain = chainInvokedChain(chainInvoked);

        filter.filter(exchange, chain).block();

        assertFalse(chainInvoked.get());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    private GatewayFilterChain chainInvokedChain(AtomicBoolean chainInvoked) {
        return exchange -> {
            chainInvoked.set(true);
            return Mono.empty();
        };
    }
}
