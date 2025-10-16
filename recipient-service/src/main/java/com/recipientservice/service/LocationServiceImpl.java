package com.recipientservice.service;

import com.recipientservice.dto.LocationDTO;
import com.recipientservice.exceptions.InvalidLocationException;
import com.recipientservice.exceptions.ResourceNotFoundException;
import com.recipientservice.kafka.EventPublisher;
import com.recipientservice.kafka.events.LocationEvent;
import com.recipientservice.model.Recipient;
import com.recipientservice.model.Location;
import com.recipientservice.repository.RecipientRepository;
import com.recipientservice.repository.LocationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    private final RecipientRepository recipientRepository;
    private final LocationRepository locationRepository;
    private final EventPublisher eventPublisher;

    public LocationServiceImpl(RecipientRepository recipientRepository,
                               LocationRepository locationRepository,
                               EventPublisher eventPublisher) {
        this.recipientRepository = recipientRepository;
        this.locationRepository = locationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LocationDTO addAddress(UUID recipientId, LocationDTO locationDTO) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        validateLocationDTO(locationDTO);

        Location location = new Location();
        BeanUtils.copyProperties(locationDTO, location);
        location.setRecipient(recipient);

        Location saved = locationRepository.save(location);

        publishLocationEventSafely(saved, recipient.getId());

        return convertToDTO(saved);
    }

    @Override
    public LocationDTO updateAddress(UUID recipientId, UUID addressId, LocationDTO locationDTO) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Location location = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateOwnership(location, recipientId);
        validateLocationDTO(locationDTO);

        BeanUtils.copyProperties(locationDTO, location, "id", "recipient");
        Location saved = locationRepository.save(location);

        publishLocationEventSafely(saved, recipient.getId());

        return convertToDTO(saved);
    }

    @Override
    public void deleteAddress(UUID recipientId, UUID addressId) {
        Location location = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateOwnership(location, recipientId);

        locationRepository.delete(location);
    }

    @Override
    public List<LocationDTO> getAddresses(UUID recipientId) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        if (recipient.getAddresses() == null || recipient.getAddresses().isEmpty()) {
            return Collections.emptyList();
        }

        return recipient.getAddresses().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDTO getAddress(UUID recipientId, UUID addressId) {
        Location location = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        validateOwnership(location, recipientId);

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

    private void validateOwnership(Location location, UUID recipientId) {
        if (location.getRecipient() == null || !location.getRecipient().getId().equals(recipientId)) {
            throw new InvalidLocationException("This address does not belong to the specified recipient");
        }
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void publishLocationEventSafely(Location location, UUID recipientId) {
        try {
            LocationEvent event = getLocationEvent(location, recipientId);
            eventPublisher.publishRecipientLocationEvent(event);
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

    private LocationEvent getLocationEvent(Location location, UUID recipientId) {
        LocationEvent event = new LocationEvent();
        BeanUtils.copyProperties(location, event);
        event.setLocationId(location.getId());
        event.setRecipientId(recipientId);
        return event;
    }
}
