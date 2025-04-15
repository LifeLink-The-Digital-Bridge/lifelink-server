package com.recipientservice.controller;

import com.recipientservice.model.Recipient;
import com.recipientservice.service.RecipientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRecipient(@RequestBody Recipient recipient) {
        return recipientService.addRecipient(recipient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipient(@PathVariable Long id) {
        return recipientService.getRecipient(id);
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from Recipient Service!";
    }
}
