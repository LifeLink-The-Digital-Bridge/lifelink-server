package com.recipientservice.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${internal.access-token}")
    private String authToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) ->
                template.header("Internal-Access-Token", authToken);
    }
}
