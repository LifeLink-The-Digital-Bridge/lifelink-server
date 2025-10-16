package com.donorservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "USER-SERVICE", path = "/users", configuration = FeignClientConfig.class)
public interface UserClient {

    @PutMapping("/{id}/add-role")
    void addRole(@PathVariable("id") UUID id, @RequestParam("role") String role);
}
