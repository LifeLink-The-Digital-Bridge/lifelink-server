package com.matchingservice.client;

import com.matchingservice.dto.CreateRecipientHistoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "RECIPIENT-SERVICE", path = "/recipients", configuration = FeignClientConfig.class)
public interface RecipientServiceClient {
    
    @PutMapping("/requests/{requestId}/status/fulfilled")
    void updateRequestStatusToFulfilled(@PathVariable UUID requestId);
    
    @PostMapping("/history/create")
    void createRecipientHistory(@RequestBody CreateRecipientHistoryRequest request);
}