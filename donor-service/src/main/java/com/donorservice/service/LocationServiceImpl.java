package com.donorservice.service;

import com.donorservice.dto.LocationDTO;
import com.donorservice.exception.InvalidLocationException;
import com.donorservice.exception.ResourceNotFoundException;
import com.donorservice.kafka.EventPublisher;
import com.donorservice.kafka.event.LocationEvent;
import com.donorservice.model.Donor;
import com.donorservice.model.Location;
import com.donorservice.repository.DonorRepository;
import com.donorservice.repository.LocationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    private final DonorRepository donorRepository;
    private final LocationRepository locationRepository;
    private final EventPublisher eventPublisher;

    public LocationServiceImpl(DonorRepository donorRepository,
                               LocationRepository locationRepository,
                               EventPublisher eventPublisher) {
        this.donorRepository = donorRepository;
        this.locationRepository = locationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LocationDTO addAddress(UUID donorId, LocationDTO locationDTO) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        validateLocationDTO(locationDTO);

        Location location = new Location();
        BeanUtils.copyProperties(locationDTO, location);
        location.setDonor(donor);

        Location saved = locationRepository.save(location);

        publishLocationEventSafely(saved, donor.getId());

        return convertToDTO(saved);
    }

    @Override
    public LocationDTO updateAddress(UUID donorId, UUID addressId, LocationDTO locationDTO) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        Location location = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateOwnership(location, donorId);
        validateLocationDTO(locationDTO);

        BeanUtils.copyProperties(locationDTO, location, "id", "donor");
        Location saved = locationRepository.save(location);

        publishLocationEventSafely(saved, donor.getId());

        return convertToDTO(saved);
    }

    @Override
    public void deleteAddress(UUID donorId, UUID addressId) {
        Location location = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateOwnership(location, donorId);

        locationRepository.delete(location);
    }

    @Override
    public List<LocationDTO> getAddresses(UUID donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        if (donor.getAddresses() == null || donor.getAddresses().isEmpty()) {
            return Collections.emptyList();
        }

        return donor.getAddresses().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDTO getAddress(UUID donorId, UUID addressId) {
        Location location = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateOwnership(location, donorId);

        return convertToDTO(location);
    }

    private void validateLocationDTO(LocationDTO locationDTO) {
        if (locationDTO == null) {
            throw new InvalidLocationException("Location data cannot be null");
        }

        validateRequiredFields(locationDTO);

        validateCoordinates(locationDTO);

        validateFormats(locationDTO);
    }

    private void validateRequiredFields(LocationDTO locationDTO) {
        if (isNullOrBlank(locationDTO.getAddressLine()) ||
                isNullOrBlank(locationDTO.getLandmark()) ||
                isNullOrBlank(locationDTO.getArea()) ||
                isNullOrBlank(locationDTO.getCity()) ||
                isNullOrBlank(locationDTO.getDistrict()) ||
                isNullOrBlank(locationDTO.getState()) ||
                isNullOrBlank(locationDTO.getCountry()) ||
                isNullOrBlank(locationDTO.getPincode()) ||
                locationDTO.getLatitude() == null ||
                locationDTO.getLongitude() == null) {
            throw new InvalidLocationException("All location fields must be provided and non-null");
        }
    }

    private void validateCoordinates(LocationDTO locationDTO) {
        if (locationDTO.getLatitude() < -90 || locationDTO.getLatitude() > 90) {
            throw new InvalidLocationException("Latitude must be between -90 and 90 degrees");
        }
        if (locationDTO.getLongitude() < -180 || locationDTO.getLongitude() > 180) {
            throw new InvalidLocationException("Longitude must be between -180 and 180 degrees");
        }
    }

    private void validateFormats(LocationDTO locationDTO) {
        if (!locationDTO.getPincode().matches("\\d{4,10}")) {
            throw new InvalidLocationException("Pincode must be 4-10 digits");
        }


        if (locationDTO.getAddressLine().length() > 255) {
            throw new InvalidLocationException("Address line cannot exceed 255 characters");
        }
        if (locationDTO.getCity().length() > 100) {
            throw new InvalidLocationException("City name cannot exceed 100 characters");
        }
        if (locationDTO.getState().length() > 100) {
            throw new InvalidLocationException("State name cannot exceed 100 characters");
        }
    }

    private void validateOwnership(Location location, UUID donorId) {
        if (location.getDonor() == null || !location.getDonor().getId().equals(donorId)) {
            throw new InvalidLocationException("This address does not belong to the specified donor");
        }
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void publishLocationEventSafely(Location location, UUID donorId) {
        try {
            LocationEvent event = getLocationEvent(location, donorId);
            eventPublisher.publishLocationEvent(event);
        } catch (Exception e) {
            System.err.println("Failed to publish location event for location " +
                    location.getId() + ": " + e.getMessage());
        }
    }

    private LocationDTO convertToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        BeanUtils.copyProperties(location, dto);
        return dto;
    }

    private LocationEvent getLocationEvent(Location location, UUID donorId) {
        LocationEvent event = new LocationEvent();
        BeanUtils.copyProperties(location, event);
        event.setLocationId(location.getId());
        event.setDonorId(donorId);
        return event;
    }
}
