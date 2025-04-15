package com.donorservice.controller;

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

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping("/add")
    public ResponseEntity<DonorDTO> createDonor(@RequestBody RegisterDonor registerDonor) {
        DonorDTO createdDonor = donorService.createDonor(registerDonor);
        return ResponseEntity.ok(createdDonor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorDTO> getDonor(@PathVariable UUID id) {
        DonorDTO donorDTO = donorService.getDonorById(id);
        return ResponseEntity.ok(donorDTO);
    }
}
