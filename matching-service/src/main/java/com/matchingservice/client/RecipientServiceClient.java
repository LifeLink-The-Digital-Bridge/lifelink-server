package com.matchingservice.client;

import com.matchingservice.enums.RequestStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "RECIPIENT-SERVICE", path = "/recipients", configuration = FeignClientConfig.class)
public interface RecipientServiceClient {

    @PutMapping("/requests/{requestId}/status")
    void updateRequestStatus(
            @PathVariable UUID requestId,
            @RequestBody RequestStatus status
    );
}