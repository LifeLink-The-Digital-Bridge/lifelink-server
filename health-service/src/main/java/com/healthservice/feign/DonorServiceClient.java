package com.healthservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "donor-service")
public interface DonorServiceClient {

    @PutMapping("/donors/addRole")
    ResponseEntity<String> addDonorRole(
            @RequestHeader("Authorization") String token,
            @RequestHeader("id") String userId
    );

    @PostMapping("/donors/profile")
    ResponseEntity<Map<String, Object>> createDonorProfile(
            @RequestHeader("Authorization") String token,
            @RequestHeader("id") String userId,
            @RequestHeader("roles") String roles,
            @RequestBody Map<String, Object> donorPayload
    );
}
