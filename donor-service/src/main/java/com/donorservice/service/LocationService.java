package com.donorservice.service;

import com.donorservice.dto.LocationDTO;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    LocationDTO addAddress(UUID donorId, LocationDTO locationDTO);
    LocationDTO updateAddress(UUID donorId, UUID addressId, LocationDTO locationDTO);
    void deleteAddress(UUID donorId, UUID addressId);
    List<LocationDTO> getAddresses(UUID donorId);
    LocationDTO getAddress(UUID donorId, UUID addressId);
}
