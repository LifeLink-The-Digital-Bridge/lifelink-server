package com.matchingservice.service;

import com.matchingservice.enums.DonorStatus;
import com.matchingservice.kafka.event.donor_events.DonationEvent;
import com.matchingservice.kafka.event.donor_events.DonorEvent;
import com.matchingservice.kafka.event.donor_events.DonorLocationEvent;
import com.matchingservice.kafka.event.recipient_events.ReceiveRequestEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientLocationEvent;
import com.matchingservice.model.*;
import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.repository.*;
import com.matchingservice.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingEventHandlerService {

    private final DonorRepository donorRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final DonationRepository donationRepository;

    private final RecipientRepository recipientRepository;
    private final RecipientLocationRepository recipientLocationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;

    private final DonorEventBuffer donorEventBuffer;
    private final DonationEventBuffer donationEventBuffer;
    private final RecipientEventBuffer recipientEventBuffer;
    private final ReceiveRequestEventBuffer receiveRequestEventBuffer;

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
        DonorLocation location = donorLocationRepository.findByLocationId(event.getLocationId()).orElse(null);
        if (location == null) {
            location = new DonorLocation();
            location.setLocationId(event.getLocationId());
        }
        location.setDonorId(event.getDonorId());
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

        var pending = donationEventBuffer.drain(location.getDonorId(), location.getLocationId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
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
        donation.setStatus(event.getStatus());

        LocationSummary summary = new LocationSummary();
        summary.setCity(location.getCity());
        summary.setState(location.getState());
        summary.setLatitude(location.getLatitude());
        summary.setLongitude(location.getLongitude());

        donation.setLocationSummary(summary);

        donationRepository.save(donation);
        System.out.println("Donation created: " + donation.getDonationId());
    }

    public void handleRecipientEvent(RecipientEvent event) {
        Recipient recipient = recipientRepository.findByRecipientId(event.getRecipientId()).orElse(null);
        if (recipient == null) {
            recipient = new Recipient();
            recipient.setRecipientId(event.getRecipientId());
        }

        recipient.setUserId(event.getUserId());
        recipient.setAvailability(event.getAvailability());
        recipient.setMedicalDetailsId(event.getMedicalDetailsId());
        recipient.setDiagnosis(event.getDiagnosis());
        recipient.setAllergies(event.getAllergies());
        recipient.setCurrentMedications(event.getCurrentMedications());
        recipient.setAdditionalNotes(event.getAdditionalNotes());
        recipient.setEligibilityCriteriaId(event.getEligibilityCriteriaId());
        recipient.setMedicallyEligible(event.getMedicallyEligible());
        recipient.setLegalClearance(event.getLegalClearance());
        recipient.setEligibilityNotes(event.getEligibilityNotes());
        recipient.setLastReviewed(event.getLastReviewed());

        System.out.println("Inside handleRecipientEvent: " + recipient.getRecipientId());
        recipientRepository.save(recipient);

        var pending = recipientEventBuffer.drain(recipient.getRecipientId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }

    public void handleRecipientLocationEvent(RecipientLocationEvent event) {
        RecipientLocation location = recipientLocationRepository.findByLocationId(event.getLocationId()).orElse(null);
        if (location == null) {
            location = new RecipientLocation();
            location.setLocationId(event.getLocationId());
        }

        location.setRecipientId(event.getRecipientId());
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

        recipientLocationRepository.save(location);

        var pending = receiveRequestEventBuffer.drain(location.getRecipientId(), location.getLocationId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }

    public void handleReceiveRequestEvent(ReceiveRequestEvent event) {
        Recipient recipient = recipientRepository.findByRecipientId(event.getRecipientId()).orElse(null);
        if (recipient == null) {
            recipientEventBuffer.buffer(event.getRecipientId(), () -> {
                System.out.println("Re-running buffered receive request event after recipient created for recipientId: " + event.getRecipientId());
                handleReceiveRequestEvent(event);
            });
            System.out.println("Recipient not found - buffering receive request event for recipientId: " + event.getRecipientId());
            return;
        }

        RecipientLocation location = null;
        if (event.getLocationId() != null) {
            location = recipientLocationRepository.findByLocationId(event.getLocationId()).orElse(null);
            if (location == null) {
                receiveRequestEventBuffer.buffer(event.getRecipientId(), event.getLocationId(), () -> {
                    System.out.println("Re-running buffered receive request event after location created for recipientId: " + event.getRecipientId() + ", locationId: " + event.getLocationId());
                    handleReceiveRequestEvent(event);
                });
                System.out.println("Location not found - buffering receive request event for recipientId: " + event.getRecipientId() + ", locationId: " + event.getLocationId());
                return;
            }
        }

        if (receiveRequestRepository.findByReceiveRequestId(event.getReceiveRequestId()).isPresent()) {
            System.out.println("Receive request already exists for receiveRequestId: " + event.getReceiveRequestId());
            return;
        }

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setReceiveRequestId(event.getReceiveRequestId());
        receiveRequest.setRecipientId(event.getRecipientId());
        receiveRequest.setRequestedBloodType(event.getRequestedBloodType());
        receiveRequest.setRequestedOrgan(event.getRequestedOrgan());
        receiveRequest.setUrgencyLevel(event.getUrgencyLevel());
        receiveRequest.setQuantity(event.getQuantity());
        receiveRequest.setRequestDate(event.getRequestDate());
        receiveRequest.setStatus(event.getStatus());
        receiveRequest.setNotes(event.getNotes());

        receiveRequestRepository.save(receiveRequest);
        System.out.println("Receive request created: " + receiveRequest.getReceiveRequestId());
    }

    private void safeRun(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
