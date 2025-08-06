package com.matchingservice.service;

import com.matchingservice.enums.DonorStatus;
import com.matchingservice.kafka.event.donor_events.DonationEvent;
import com.matchingservice.kafka.event.donor_events.DonorEvent;
import com.matchingservice.kafka.event.donor_events.DonorLocationEvent;
import com.matchingservice.kafka.event.recipient_events.ReceiveRequestEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientEvent;
import com.matchingservice.model.*;
import com.matchingservice.repository.DonationRepository;
import com.matchingservice.repository.DonorLocationRepository;
import com.matchingservice.repository.DonorRepository;
import com.matchingservice.utils.DonationEventBuffer;
import com.matchingservice.utils.DonorEventBuffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingEventHandlerService {

    private final DonorRepository donorRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final DonationRepository donationRepository;
    private final DonorEventBuffer donorEventBuffer;
    private final DonationEventBuffer donationEventBuffer;

    public void handleDonorEvent(DonorEvent event) {
        Donor donor = donorRepository.findByDonorId(event.getDonorId()).orElse(null);
        if (donor == null) {
            donor = new Donor();
            donor.setDonorId(event.getDonorId());
        }
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

        System.out.println("Inside handleDonorEvent: " + donor.getDonorId());
        donorRepository.save(donor);

        var pending = donorEventBuffer.drain(donor.getDonorId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }

    public void handleDonorLocationEvent(DonorLocationEvent event) {

    }

    public void handleDonationEvent(DonationEvent event) {
        Donor donor = donorRepository.findByDonorId(event.getDonorId()).orElse(null);
        if (donor == null) {
            donorEventBuffer.buffer(event.getDonorId(), () -> {
                System.out.println("Re-running buffered donation event after donor created for donorId: " + event.getDonorId());
                handleDonationEvent(event);
            });
            System.out.println("Donor not found - buffering donation event for donorId: " + event.getDonorId());
            return;
        }
        DonorLocation location = donorLocationRepository.findByLocationId(event.getLocationId()).orElse(null);
        if (location == null) {
            donationEventBuffer.buffer(event.getDonorId(), event.getLocationId(), () -> {
                System.out.println("Re-running buffered donation event after location created for donorId: " + event.getDonorId() + ", locationId: " + event.getLocationId());
                handleDonationEvent(event);
            });
            System.out.println("Location not found - buffering donation event for donorId: " + event.getDonorId() + ", locationId: " + event.getLocationId());
            return;
        }
        if (donationRepository.findByDonationId(event.getDonationId()).isPresent()) {
            System.out.println("Donation already exists for donationId: " + event.getDonationId());
            return;
        }

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
        System.out.println("Donation created: " + donation.getDonationId());
    }

    private void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRecipientEvent(RecipientEvent event) {
    }

    public void handleReceiveRequestEvent(ReceiveRequestEvent event) {
    }
}
