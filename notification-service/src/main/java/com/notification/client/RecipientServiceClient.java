package com.notification.client;

import com.notification.dto.RecipientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "RECIPIENT-SERVICE")
public interface RecipientServiceClient {

    @GetMapping("/recipients/internal/{id}")
    RecipientDTO getRecipientById(@PathVariable("id") UUID id, @RequestHeader("Internal-Access-Token") String internalToken);
}
