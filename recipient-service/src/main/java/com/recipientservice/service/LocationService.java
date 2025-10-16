package com.recipientservice.service;

import com.recipientservice.dto.LocationDTO;
import java.util.List;
import java.util.UUID;

public interface LocationService {
    LocationDTO addAddress(UUID recipientId, LocationDTO locationDTO);
    LocationDTO updateAddress(UUID recipientId, UUID addressId, LocationDTO locationDTO);
    void deleteAddress(UUID recipientId, UUID addressId);
    List<LocationDTO> getAddresses(UUID recipientId);
    LocationDTO getAddress(UUID recipientId, UUID addressId);
}
