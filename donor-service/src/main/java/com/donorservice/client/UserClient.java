package com.donorservice.client;

import com.donorservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "USER-SERVICE", path = "/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable UUID id);
}
