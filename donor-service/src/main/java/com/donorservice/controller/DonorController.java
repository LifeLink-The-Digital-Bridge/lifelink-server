package com.donorservice.controller;

import com.donorservice.aop.RequireRole;
import com.donorservice.client.UserClient;
import com.donorservice.dto.DonorDTO;
import com.donorservice.dto.RegisterDonor;
import com.donorservice.service.DonorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;
    private final UserClient userClient;


    public DonorController(DonorService donorService, UserClient userClient) {
        this.donorService = donorService;
        this.userClient = userClient;
    }

    @PostMapping("/addRole")
    public ResponseEntity<String> addRole(@RequestHeader("id") String userId) {
        userClient.addRole(UUID.fromString(userId), "DONOR");
        return ResponseEntity.ok("Role added");
    }

    @RequireRole("DONOR")
    @PostMapping("/register")
    public ResponseEntity<DonorDTO> createDonor(@RequestHeader("id") String userId, @RequestBody RegisterDonor registerDonor) {
        DonorDTO createdDonor = donorService.createDonor(UUID.fromString(userId), registerDonor);
        return ResponseEntity.ok(createdDonor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorDTO> getDonor(@PathVariable UUID id) {
        DonorDTO donorDTO = donorService.getDonorById(id);
        return ResponseEntity.ok(donorDTO);
    }
}
