package com.matchingservice.service;

import com.matchingservice.enums.DonorStatus;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.kafka.event.donor_events.DonationEvent;
import com.matchingservice.kafka.event.donor_events.DonorEvent;
import com.matchingservice.kafka.event.donor_events.DonorHLAProfileEvent;
import com.matchingservice.kafka.event.donor_events.DonorLocationEvent;
import com.matchingservice.kafka.event.recipient_events.ReceiveRequestEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientHLAProfileEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientLocationEvent;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.model.recipients.RecipientHLAProfile;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.repository.*;
import com.matchingservice.utils.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchingEventHandlerService {

    private final DonorRepository donorRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final DonationRepository donationRepository;

    private final RecipientRepository recipientRepository;
    private final RecipientLocationRepository recipientLocationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final DonorHLAProfileRepository donorHLAProfileRepository;

    private final DonorEventBuffer donorEventBuffer;
    private final DonationEventBuffer donationEventBuffer;
    private final RecipientEventBuffer recipientEventBuffer;
    private final ReceiveRequestEventBuffer receiveRequestEventBuffer;
    private final RecipientHLAProfileRepository recipientHLAProfileRepository;

    public void handleDonorEvent(DonorEvent event) {
        Donor donor = donorRepository.findById(event.getDonorId()).orElse(null);
        if (donor == null) {
            donor = new Donor();
            donor.setDonorId(event.getDonorId());
        }

        donor.setUserId(event.getUserId());
        donor.setRegistrationDate(event.getRegistrationDate());

        donor.setStatus(DonorStatus.valueOf(event.getStatus()));

        donor.setWeight(event.getWeight());
        donor.setAge(event.getAge());
        donor.setDob(event.getDob());
        donor.setMedicalClearance(event.getMedicalClearance());
        donor.setRecentTattooOrPiercing(event.getRecentTattooOrPiercing());
        donor.setRecentTravelDetails(event.getRecentTravelDetails());
        donor.setRecentVaccination(event.getRecentVaccination());
        donor.setRecentSurgery(event.getRecentSurgery());
        donor.setChronicDiseases(event.getChronicDiseases());
        donor.setAllergies(event.getAllergies());
        donor.setLastDonationDate(event.getLastDonationDate());
        donor.setHeight(event.getHeight());
        donor.setBodyMassIndex(event.getBodyMassIndex());
        donor.setBodySize(event.getBodySize());
        donor.setIsLivingDonor(event.getIsLivingDonor());

        donor.setHemoglobinLevel(event.getHemoglobinLevel());
        donor.setBloodPressure(event.getBloodPressure());
        donor.setHasDiseases(event.getHasDiseases());
        donor.setTakingMedication(event.getTakingMedication());
        donor.setDiseaseDescription(event.getDiseaseDescription());

        donor.setCurrentMedications(event.getCurrentMedications());
        donor.setLastMedicalCheckup(event.getLastMedicalCheckup());
        donor.setMedicalHistory(event.getMedicalHistory());
        donor.setHasInfectiousDiseases(event.getHasInfectiousDiseases());
        donor.setInfectiousDiseaseDetails(event.getInfectiousDiseaseDetails());
        donor.setCreatinineLevel(event.getCreatinineLevel());
        donor.setLiverFunctionTests(event.getLiverFunctionTests());
        donor.setCardiacStatus(event.getCardiacStatus());
        donor.setPulmonaryFunction(event.getPulmonaryFunction());
        donor.setOverallHealthStatus(event.getOverallHealthStatus());

        System.out.println("Inside handleDonorEvent: " + donor.getDonorId());
        donorRepository.save(donor);

        var pending = donorEventBuffer.drain(donor.getDonorId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }

    public void handleDonorLocationEvent(DonorLocationEvent event) {
        DonorLocation location = donorLocationRepository.findById(event.getLocationId()).orElse(null);
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
        Donor donor = donorRepository.findById(event.getDonorId()).orElse(null);
        if (donor == null) {
            donorEventBuffer.buffer(event.getDonorId(), () -> {
                System.out.println("Re-running buffered donation event after donor created for donorId: " + event.getDonorId());
                handleDonationEvent(event);
            });
            System.out.println("Donor not found - buffering donation event for donorId: " + event.getDonorId());
            return;
        }

        DonorLocation location = donorLocationRepository.findById(event.getLocationId()).orElse(null);
        if (location == null) {
            donationEventBuffer.buffer(event.getDonorId(), event.getLocationId(), () -> {
                System.out.println("Re-running buffered donation event after location created for donorId: " + event.getDonorId() + ", locationId: " + event.getLocationId());
                handleDonationEvent(event);
            });
            System.out.println("Location not found - buffering donation event for donorId: " + event.getDonorId() + ", locationId: " + event.getLocationId());
            return;
        }

        if (donationRepository.findById(event.getDonationId()).isPresent()) {
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
                d.setOrganQuality(event.getOrganQuality());
                d.setOrganViabilityExpiry(event.getOrganViabilityExpiry());
                d.setColdIschemiaTime(event.getColdIschemiaTime());
                d.setOrganPerfused(event.getOrganPerfused());
                d.setOrganWeight(event.getOrganWeight());
                d.setOrganSize(event.getOrganSize());
                d.setFunctionalAssessment(event.getFunctionalAssessment());
                d.setHasAbnormalities(event.getHasAbnormalities());
                d.setAbnormalityDescription(event.getAbnormalityDescription());
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
        donation.setDonorId(event.getDonorId());
        donation.setUserId(donor.getUserId());
        donation.setLocationId(event.getLocationId());
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
        Recipient recipient = recipientRepository.findById(event.getRecipientId()).orElse(null);
        if (recipient == null) {
            recipient = new Recipient();
            recipient.setRecipientId(event.getRecipientId());
        }

        recipient.setUserId(event.getUserId());
        recipient.setAvailability(event.getAvailability());

        recipient.setMedicalDetailsId(event.getMedicalDetailsId());
        recipient.setHemoglobinLevel(event.getHemoglobinLevel());
        recipient.setBloodPressure(event.getBloodPressure());
        recipient.setDiagnosis(event.getDiagnosis());
        recipient.setAllergies(event.getAllergies());
        recipient.setCurrentMedications(event.getCurrentMedications());
        recipient.setAdditionalNotes(event.getAdditionalNotes());
        recipient.setHasInfectiousDiseases(event.getHasInfectiousDiseases());
        recipient.setInfectiousDiseaseDetails(event.getInfectiousDiseaseDetails());
        recipient.setCreatinineLevel(event.getCreatinineLevel());
        recipient.setLiverFunctionTests(event.getLiverFunctionTests());
        recipient.setCardiacStatus(event.getCardiacStatus());
        recipient.setPulmonaryFunction(event.getPulmonaryFunction());
        recipient.setOverallHealthStatus(event.getOverallHealthStatus());

        recipient.setEligibilityCriteriaId(event.getEligibilityCriteriaId());
        recipient.setAgeEligible(event.getAgeEligible());
        recipient.setAge(event.getAge());
        recipient.setDob(event.getDob());
        recipient.setWeightEligible(event.getWeightEligible());
        recipient.setWeight(event.getWeight());
        recipient.setMedicallyEligible(event.getMedicallyEligible());
        recipient.setLegalClearance(event.getLegalClearance());
        recipient.setEligibilityNotes(event.getEligibilityNotes());
        recipient.setLastReviewed(event.getLastReviewed());
        recipient.setHeight(event.getHeight());
        recipient.setBodyMassIndex(event.getBodyMassIndex());
        recipient.setBodySize(event.getBodySize());
        recipient.setIsLivingDonor(event.getIsLivingDonor());

        System.out.println("Inside handleRecipientEvent: " + recipient.getRecipientId());
        recipientRepository.save(recipient);

        var pending = recipientEventBuffer.drain(recipient.getRecipientId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }


    public void handleRecipientLocationEvent(RecipientLocationEvent event) {
        RecipientLocation location = recipientLocationRepository.findById(event.getLocationId()).orElse(null);
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
        Recipient recipient = recipientRepository.findById(event.getRecipientId()).orElse(null);
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
            location = recipientLocationRepository.findById(event.getLocationId()).orElse(null);
            if (location == null) {
                receiveRequestEventBuffer.buffer(event.getRecipientId(), event.getLocationId(), () -> {
                    System.out.println("Re-running buffered receive request event after location created for recipientId: " + event.getRecipientId() + ", locationId: " + event.getLocationId());
                    handleReceiveRequestEvent(event);
                });
                System.out.println("Location not found - buffering receive request event for recipientId: " + event.getRecipientId() + ", locationId: " + event.getLocationId());
                return;
            }
        }

        if (receiveRequestRepository.findById(event.getReceiveRequestId()).isPresent()) {
            System.out.println("Receive request already exists for receiveRequestId: " + event.getReceiveRequestId());
            return;
        }

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setReceiveRequestId(event.getReceiveRequestId());
        receiveRequest.setRecipientId(event.getRecipientId());
        receiveRequest.setUserId(recipient.getUserId());
        receiveRequest.setLocationId(event.getLocationId());
        receiveRequest.setRequestType(event.getRequestType());
        receiveRequest.setRequestedBloodType(event.getRequestedBloodType());
        receiveRequest.setRequestedOrgan(event.getRequestedOrgan());
        receiveRequest.setRequestedTissue(event.getRequestedTissue());
        receiveRequest.setRequestedStemCellType(event.getRequestedStemCellType());
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

    @Transactional
    public void handleDonorHLAProfileEvent(DonorHLAProfileEvent event) {
        System.out.println("Processing HLA Profile Event for donor: " + event.getDonorId());

        Optional<DonorHLAProfile> existingProfileOptional = donorHLAProfileRepository.findById(event.getId());

        DonorHLAProfile profile = getDonorHLAProfile(event, existingProfileOptional);

        DonorHLAProfile savedProfile = donorHLAProfileRepository.save(profile);

        if (existingProfileOptional.isPresent()) {
            System.out.println("Updated existing HLA profile for donor: " + event.getDonorId());
        } else {
            System.out.println("Created new HLA profile for donor: " + event.getDonorId());
        }

    }

    private static DonorHLAProfile getDonorHLAProfile(DonorHLAProfileEvent event, Optional<DonorHLAProfile> existingProfileOptional) {
        DonorHLAProfile profile = existingProfileOptional.orElse(new DonorHLAProfile());

        profile.setId(event.getId());
        profile.setDonorId(event.getDonorId());
        profile.setHlaA1(event.getHlaA1());
        profile.setHlaA2(event.getHlaA2());
        profile.setHlaB1(event.getHlaB1());
        profile.setHlaB2(event.getHlaB2());
        profile.setHlaC1(event.getHlaC1());
        profile.setHlaC2(event.getHlaC2());
        profile.setHlaDR1(event.getHlaDR1());
        profile.setHlaDR2(event.getHlaDR2());
        profile.setHlaDQ1(event.getHlaDQ1());
        profile.setHlaDQ2(event.getHlaDQ2());
        profile.setHlaDP1(event.getHlaDP1());
        profile.setHlaDP2(event.getHlaDP2());
        profile.setTestingDate(event.getTestingDate());
        profile.setTestingMethod(event.getTestingMethod());
        profile.setLaboratoryName(event.getLaboratoryName());
        profile.setCertificationNumber(event.getCertificationNumber());
        profile.setHlaString(event.getHlaString());
        profile.setIsHighResolution(event.getIsHighResolution());
        return profile;
    }

    @Transactional
    public void handleRecipientHLAProfileEvent(RecipientHLAProfileEvent event) {
        System.out.println("Processing HLA Profile Event for recipient: " + event.getRecipientId());

        Optional<RecipientHLAProfile> existingProfileOptional = recipientHLAProfileRepository.findById(event.getId());

        RecipientHLAProfile profile = getRecipientHLAProfile(event, existingProfileOptional);

        RecipientHLAProfile savedProfile = recipientHLAProfileRepository.save(profile);

        if (existingProfileOptional.isPresent()) {
            System.out.println("Updated existing HLA profile for recipient: " + event.getRecipientId());
        } else {
            System.out.println("Created new HLA profile for recipient: " + event.getRecipientId());
        }
    }

    private static RecipientHLAProfile getRecipientHLAProfile(RecipientHLAProfileEvent event,
                                                              Optional<RecipientHLAProfile> existingProfileOptional) {
        RecipientHLAProfile profile = existingProfileOptional.orElse(new RecipientHLAProfile());

        profile.setId(event.getId());
        profile.setRecipientId(event.getRecipientId());
        profile.setHlaA1(event.getHlaA1());
        profile.setHlaA2(event.getHlaA2());
        profile.setHlaB1(event.getHlaB1());
        profile.setHlaB2(event.getHlaB2());
        profile.setHlaC1(event.getHlaC1());
        profile.setHlaC2(event.getHlaC2());
        profile.setHlaDR1(event.getHlaDR1());
        profile.setHlaDR2(event.getHlaDR2());
        profile.setHlaDQ1(event.getHlaDQ1());
        profile.setHlaDQ2(event.getHlaDQ2());
        profile.setHlaDP1(event.getHlaDP1());
        profile.setHlaDP2(event.getHlaDP2());
        profile.setTestingDate(event.getTestingDate());
        profile.setTestingMethod(event.getTestingMethod());
        profile.setLaboratoryName(event.getLaboratoryName());
        profile.setCertificationNumber(event.getCertificationNumber());
        profile.setHlaString(event.getHlaString());
        profile.setIsHighResolution(event.getIsHighResolution());

        return profile;
    }

}
