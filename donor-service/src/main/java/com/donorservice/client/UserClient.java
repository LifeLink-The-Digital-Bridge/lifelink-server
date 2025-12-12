package com.donorservice.client;

import com.donorservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "USER-SERVICE", path = "/users", configuration = FeignClientConfig.class)
public interface UserClient {

    @PutMapping("/{id}/add-role")
    void addRole(@PathVariable("id") UUID id, @RequestParam("role") String role);

    @PostMapping("/batch")
    List<UserDTO> getUsersByIds(@RequestBody List<UUID> userIds);
}
