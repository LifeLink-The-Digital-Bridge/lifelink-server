package com.matchingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@FeignClient(name = "RECIPIENT-SERVICE", path = "/recipients", configuration = FeignClientConfig.class)
public interface RecipientServiceClient {
    
    @PutMapping("/requests/{requestId}/status/fulfilled")
    void updateRequestStatusToFulfilled(@PathVariable UUID requestId);
}