package com.recipientservice.controller;

import com.recipientservice.aop.RequireRole;
import com.recipientservice.dto.LocationDTO;
import com.recipientservice.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recipients/{recipientId}/addresses")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @RequireRole("RECIPIENT")
    @PostMapping
    public ResponseEntity<LocationDTO> addAddress(@PathVariable UUID recipientId,
                                                  @RequestBody @Valid LocationDTO locationDTO) {
        return ResponseEntity.ok(locationService.addAddress(recipientId, locationDTO));
    }

    @RequireRole("RECIPIENT")
    @PutMapping("/{addressId}")
    public ResponseEntity<LocationDTO> updateAddress(@PathVariable UUID recipientId,
                                                     @PathVariable UUID addressId,
                                                     @RequestBody @Valid LocationDTO locationDTO) {
        return ResponseEntity.ok(locationService.updateAddress(recipientId, addressId, locationDTO));
    }

    @RequireRole("RECIPIENT")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID recipientId,
                                              @PathVariable UUID addressId) {
        locationService.deleteAddress(recipientId, addressId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole("RECIPIENT")
    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAddresses(@PathVariable UUID recipientId) {
        return ResponseEntity.ok(locationService.getAddresses(recipientId));
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/{addressId}")
    public ResponseEntity<LocationDTO> getAddress(@PathVariable UUID recipientId,
                                                  @PathVariable UUID addressId) {
        return ResponseEntity.ok(locationService.getAddress(recipientId, addressId));
    }
}
