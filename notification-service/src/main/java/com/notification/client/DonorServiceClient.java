package com.notification.client;

import com.notification.dto.DonorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "DONOR-SERVICE")
public interface DonorServiceClient {

    @GetMapping("/donors/internal/{id}")
    DonorDTO getDonorById(@PathVariable("id") UUID id, @RequestHeader("Internal-Access-Token") String internalToken);
}
