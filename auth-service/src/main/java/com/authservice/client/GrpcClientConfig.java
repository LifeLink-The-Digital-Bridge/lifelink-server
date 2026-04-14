package com.authservice.client;

import com.userservice.grpc.UserServiceGrpc;
import io.grpc.ClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Value("${internal.access-token}")
    private String authToken;

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(GrpcChannelFactory channels) {
        return UserServiceGrpc.newBlockingStub(channels.createChannel("user-service"));
    }

    @GlobalClientInterceptor
    @Bean
    public ClientInterceptor headerInterceptor() {
        return new GrpcClientHeaderInterceptor("Internal-Access-Token", authToken);
    }
}

