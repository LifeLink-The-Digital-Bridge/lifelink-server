package com.matchingservice.client;

import com.matchingservice.dto.CreateDonationHistoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "DONOR-SERVICE", path = "/donors", configuration = FeignClientConfig.class)
public interface DonorServiceClient {
    
    @PutMapping("/donations/{donationId}/status/completed")
    void updateDonationStatusToCompleted(@PathVariable UUID donationId);
    
    @PostMapping("/history/create")
    void createDonationHistory(@RequestBody CreateDonationHistoryRequest request);
}