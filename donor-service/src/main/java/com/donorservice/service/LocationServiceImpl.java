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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    private final DonorRepository donorRepository;
    private final LocationRepository locationRepository;
    private final EventPublisher eventPublisher;

    public LocationServiceImpl(DonorRepository donorRepository, LocationRepository locationRepository, EventPublisher eventPublisher) {
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
        location.setId(UUID.randomUUID());
        location.setDonor(donor);

        Location saved = locationRepository.save(location);

        if (donor.getAddresses() == null)
            donor.setAddresses(new ArrayList<>());
        donor.getAddresses().add(saved);
        donorRepository.save(donor);

        eventPublisher.publishLocationEvent(getLocationEvent(saved, donor.getId()));

        LocationDTO result = new LocationDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public LocationDTO updateAddress(UUID donorId, UUID addressId, LocationDTO locationDTO) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        Location address = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (address.getDonor() == null || !address.getDonor().getId().equals(donorId)) {
            throw new InvalidLocationException("This address does not belong to the donor.");
        }
        validateLocationDTO(locationDTO);

        BeanUtils.copyProperties(locationDTO, address, "id", "donor");
        Location saved = locationRepository.save(address);

        eventPublisher.publishLocationEvent(getLocationEvent(saved, donor.getId()));

        LocationDTO result = new LocationDTO();
        BeanUtils.copyProperties(saved, result);
        return result;
    }

    @Override
    public void deleteAddress(UUID donorId, UUID addressId) {
        Location address = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (address.getDonor() == null || !address.getDonor().getId().equals(donorId)) {
            throw new InvalidLocationException("This address does not belong to the donor.");
        }
        locationRepository.delete(address);
    }

    @Override
    public List<LocationDTO> getAddresses(UUID donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
        List<LocationDTO> dtos = new ArrayList<>();
        if (donor.getAddresses() != null) {
            for (Location location : donor.getAddresses()) {
                LocationDTO dto = new LocationDTO();
                BeanUtils.copyProperties(location, dto);
                dtos.add(dto);
            }
        }
        return dtos;
    }

    @Override
    public LocationDTO getAddress(UUID donorId, UUID addressId) {
        Location address = locationRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (address.getDonor() == null || !address.getDonor().getId().equals(donorId)) {
            throw new InvalidLocationException("This address does not belong to the donor.");
        }
        LocationDTO dto = new LocationDTO();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }

    private void validateLocationDTO(LocationDTO locDTO) {
        if (locDTO.getAddressLine() == null || locDTO.getLandmark() == null ||
                locDTO.getArea() == null || locDTO.getCity() == null ||
                locDTO.getDistrict() == null || locDTO.getState() == null ||
                locDTO.getCountry() == null || locDTO.getPincode() == null ||
                locDTO.getLatitude() == null || locDTO.getLongitude() == null) {
            throw new InvalidLocationException("All location fields must be provided and non-null.");
        }
    }

    private LocationEvent getLocationEvent(Location location, UUID donorId) {
        LocationEvent event = new LocationEvent();
        BeanUtils.copyProperties(location, event);
        event.setLocationId(location.getId());
        event.setDonorId(donorId);
        return event;
    }
}
