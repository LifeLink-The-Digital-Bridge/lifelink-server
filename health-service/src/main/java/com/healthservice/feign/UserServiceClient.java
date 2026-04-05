package com.healthservice.feign;

import com.healthservice.dto.UserAnalyticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PutMapping("/users/{userId}/add-role")
    ResponseEntity<Map<String, Object>> addRole(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") UUID userId,
            @RequestBody Map<String, String> roleRequest
    );

    @GetMapping("/users/admin/analytics")
    UserAnalyticsDTO getAdminAnalytics(
            @RequestHeader("Authorization") String token,
            @RequestHeader("roles") String rolesHeader
    );
}
