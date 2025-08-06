package com.donorservice.controller;

import com.donorservice.aop.RequireRole;
import com.donorservice.dto.LocationDTO;
import com.donorservice.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/donors/{donorId}/addresses")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @RequireRole("DONOR")
    @PostMapping
    public ResponseEntity<LocationDTO> addAddress(@PathVariable UUID donorId, @RequestBody LocationDTO locationDTO) {
        return ResponseEntity.ok(locationService.addAddress(donorId, locationDTO));
    }

    @RequireRole("DONOR")
    @PutMapping("/{addressId}")
    public ResponseEntity<LocationDTO> updateAddress(@PathVariable UUID donorId, @PathVariable UUID addressId, @RequestBody LocationDTO locationDTO) {
        return ResponseEntity.ok(locationService.updateAddress(donorId, addressId, locationDTO));
    }

    @RequireRole("DONOR")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID donorId, @PathVariable UUID addressId) {
        locationService.deleteAddress(donorId, addressId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole("DONOR")
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAddresses(@PathVariable UUID donorId) {
        return ResponseEntity.ok(locationService.getAddresses(donorId));
    }

    @RequireRole("DONOR")
    @GetMapping("/{addressId}")
    public ResponseEntity<LocationDTO> getAddress(@PathVariable UUID donorId, @PathVariable UUID addressId) {
        return ResponseEntity.ok(locationService.getAddress(donorId, addressId));
    }
}
