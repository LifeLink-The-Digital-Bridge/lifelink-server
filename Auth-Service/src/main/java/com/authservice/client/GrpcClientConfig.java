package com.authservice.client;

import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${internal.access-token}")
    private String authToken;

    @GrpcGlobalClientInterceptor
    @Bean
    public GrpcClientHeaderInterceptor headerInterceptor() {
        return new GrpcClientHeaderInterceptor("Internal-Access-Token", authToken);
    }
}

