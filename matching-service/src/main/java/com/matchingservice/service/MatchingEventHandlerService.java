package com.matchingservice.service;

import com.matchingservice.enums.DonorStatus;
import com.matchingservice.kafka.event.DonationEvent;
import com.matchingservice.kafka.event.DonorEvent;
import com.matchingservice.kafka.event.LocationEvent;
import com.matchingservice.model.*;
import com.matchingservice.repository.DonationRepository;
import com.matchingservice.repository.DonorLocationRepository;
import com.matchingservice.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingEventHandlerService {

    private final DonorRepository donorRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final DonationRepository donationRepository;

    public void handleDonorEvent(DonorEvent event) {
        Donor donor = new Donor();
        donor.setDonorId(event.getDonorId());
        donor.setUserId(event.getUserId());
        donor.setRegistrationDate(event.getRegistrationDate());
        donor.setStatus(DonorStatus.valueOf(event.getStatus()));
        donor.setWeight(event.getWeight());
        donor.setAge(event.getAge());
        donor.setMedicalClearance(event.getMedicalClearance());
        donor.setRecentSurgery(event.getRecentSurgery());
        donor.setChronicDiseases(event.getChronicDiseases());
        donor.setAllergies(event.getAllergies());
        donor.setLastDonationDate(event.getLastDonationDate());
        donor.setHemoglobinLevel(event.getHemoglobinLevel());
        donor.setBloodPressure(event.getBloodPressure());
        donor.setHasDiseases(event.getHasDiseases());
        donor.setTakingMedication(event.getTakingMedication());
        donor.setDiseaseDescription(event.getDiseaseDescription());

        donorRepository.save(donor);
    }

    public void handleLocationEvent(LocationEvent event) {
        Donor donor = donorRepository.findById(event.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        DonorLocation location = new DonorLocation();
        location.setId(event.getLocationId());
        location.setDonor(donor);
        location.setAddressLine(event.getAddressLine());
        location.setLandmark(event.getLandmark());
        location.setArea(event.getArea());
        location.setCity(event.getCity());
        location.setDistrict(event.getDistrict());
        location.setState(event.getState());
        location.setCountry(event.getCountry());
        location.setPincode(event.getPincode());
        location.setLatitude(event.getLatitude());
        location.setLongitude(event.getLongitude());

        donorLocationRepository.save(location);
    }

    public void handleDonationEvent(DonationEvent event) {
        Donor donor = donorRepository.findById(event.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found"));
        DonorLocation location = donorLocationRepository.findById(event.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        Donation donation = switch (event.getDonationType()) {
            case BLOOD -> {
                BloodDonation d = new BloodDonation();
                d.setQuantity(event.getQuantity());
                yield d;
            }
            case ORGAN -> {
                OrganDonation d = new OrganDonation();
                d.setOrganType(event.getOrganType());
                d.setIsCompatible(event.getIsCompatible());
                yield d;
            }
            case TISSUE -> {
                TissueDonation d = new TissueDonation();
                d.setTissueType(event.getTissueType());
                d.setQuantity(event.getQuantity());
                yield d;
            }
            case STEM_CELL -> {
                StemCellDonation d = new StemCellDonation();
                d.setStemCellType(event.getStemCellType());
                d.setQuantity(event.getQuantity());
                yield d;
            }
        };

        donation.setDonationId(event.getDonationId());
        donation.setDonor(donor);
        donation.setLocation(location);
        donation.setDonationType(event.getDonationType());
        donation.setDonationDate(event.getDonationDate());
        donation.setBloodType(event.getBloodType());

        LocationSummary summary = new LocationSummary();
        summary.setCity(location.getCity());
        summary.setState(location.getState());
        summary.setLatitude(location.getLatitude());
        summary.setLongitude(location.getLongitude());

        donation.setLocationSummary(summary);
        donationRepository.save(donation);
    }
}

