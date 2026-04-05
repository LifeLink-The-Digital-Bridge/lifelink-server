package com.healthservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "recipient-service")
public interface RecipientServiceClient {

    @PutMapping("/recipients/addRole")
    ResponseEntity<String> addRecipientRole(
            @RequestHeader("Authorization") String token,
            @RequestHeader("id") String userId
    );

    @PostMapping("/recipients/profile")
    ResponseEntity<Map<String, Object>> createRecipientProfile(
            @RequestHeader("Authorization") String token,
            @RequestHeader("id") String userId,
            @RequestHeader("roles") String roles,
            @RequestBody Map<String, Object> recipientPayload
    );
}
