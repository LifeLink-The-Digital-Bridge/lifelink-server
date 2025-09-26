package com.matchingservice.service;

import com.matchingservice.enums.*;
import com.matchingservice.kafka.event.donor_events.*;
import com.matchingservice.kafka.event.recipient_events.*;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.*;
import com.matchingservice.repository.donor.*;
import com.matchingservice.repository.recipient.*;
import com.matchingservice.utils.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchingEventHandlerService {

    private final DonorRepository donorRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final DonationRepository donationRepository;
    private final DonorHLAProfileRepository donorHLAProfileRepository;

    private final RecipientRepository recipientRepository;
    private final RecipientLocationRepository recipientLocationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final RecipientHLAProfileRepository recipientHLAProfileRepository;

    private final DonorEventBuffer donorEventBuffer;
    private final DonationEventBuffer donationEventBuffer;
    private final RecipientEventBuffer recipientEventBuffer;
    private final ReceiveRequestEventBuffer receiveRequestEventBuffer;

    @Transactional
    public void handleDonorEvent(DonorEvent event) {
        System.out.println("Processing DonorEvent for donorId: " + event.getDonorId());

        Donor donor = new Donor();
        donor.setDonorId(event.getDonorId());
        donor.setUserId(event.getUserId());
        donor.setRegistrationDate(event.getRegistrationDate());
        donor.setStatus(DonorStatus.valueOf(event.getStatus()));
        donor.setEventTimestamp(LocalDateTime.now());

        if (event.getEligibilityCriteriaId() != null) {
            DonorEligibilityCriteria eligibility = new DonorEligibilityCriteria();
            eligibility.setEligibilityCriteriaId(event.getEligibilityCriteriaId());
            eligibility.setDonor(donor);
            eligibility.setWeight(event.getWeight());
            eligibility.setAge(event.getAge());
            eligibility.setDob(event.getDob());
            eligibility.setMedicalClearance(event.getMedicalClearance());
            eligibility.setRecentTattooOrPiercing(event.getRecentTattooOrPiercing());
            eligibility.setRecentTravelDetails(event.getRecentTravelDetails());
            eligibility.setRecentVaccination(event.getRecentVaccination());
            eligibility.setRecentSurgery(event.getRecentSurgery());
            eligibility.setChronicDiseases(event.getChronicDiseases());
            eligibility.setAllergies(event.getAllergies());
            eligibility.setLastDonationDate(event.getLastDonationDate());
            eligibility.setHeight(event.getHeight());
            eligibility.setBodyMassIndex(event.getBodyMassIndex());
            eligibility.setBodySize(event.getBodySize());
            eligibility.setIsLivingDonor(event.getIsLivingDonor());
            eligibility.setSmokingStatus(event.getSmokingStatus());
            eligibility.setPackYears(event.getPackYears());
            eligibility.setQuitSmokingDate(event.getQuitSmokingDate());
            eligibility.setAlcoholStatus(event.getAlcoholStatus());
            eligibility.setDrinksPerWeek(event.getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(event.getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(event.getAlcoholAbstinenceMonths());
            donor.setEligibilityCriteria(eligibility);
        }

        if (event.getMedicalDetailsId() != null) {
            DonorMedicalDetails medicalDetails = new DonorMedicalDetails();
            medicalDetails.setMedicalDetailsId(event.getMedicalDetailsId());
            medicalDetails.setDonor(donor);
            medicalDetails.setHemoglobinLevel(event.getHemoglobinLevel());
            medicalDetails.setBloodPressure(event.getBloodPressure());
            medicalDetails.setHasDiseases(event.getHasDiseases());
            medicalDetails.setTakingMedication(event.getTakingMedication());
            medicalDetails.setDiseaseDescription(event.getDiseaseDescription());
            medicalDetails.setCurrentMedications(event.getCurrentMedications());
            medicalDetails.setLastMedicalCheckup(event.getLastMedicalCheckup());
            medicalDetails.setMedicalHistory(event.getMedicalHistory());
            medicalDetails.setHasInfectiousDiseases(event.getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(event.getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(event.getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(event.getLiverFunctionTests());
            medicalDetails.setCardiacStatus(event.getCardiacStatus());
            medicalDetails.setPulmonaryFunction(event.getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(event.getOverallHealthStatus());
            donor.setMedicalDetails(medicalDetails);
        }

        Donor savedDonor = donorRepository.save(donor);
        System.out.println("Created new donor history snapshot: " + savedDonor.getDonorId()
                + " at timestamp: " + savedDonor.getEventTimestamp());

        var pendingEvents = donorEventBuffer.drain(donor.getDonorId());
        if (pendingEvents != null) {
            pendingEvents.forEach(this::safeRun);
        }
    }

    @Transactional
    public void handleDonationEvent(DonationEvent event) {
        System.out.println("Processing DonationEvent for donationId: " + event.getDonationId());

        Donor donor = donorRepository.findTopByDonorIdOrderByEventTimestampDesc(event.getDonorId())
                .orElse(null);

        if (donor == null) {
            System.out.println("Donor not found, buffering donation event: " + event.getDonorId());
            donationEventBuffer.buffer(event.getDonorId(), event.getLocationId(), () -> handleDonationEvent(event));
            return;
        }

        Donation donation = createDonationByType(event);
        donation.setDonationId(event.getDonationId());
        donation.setDonor(donor);
        donation.setDonorId(donor.getDonorId());
        donation.setUserId(event.getDonorId());
        donation.setDonationType(event.getDonationType());
        donation.setBloodType(event.getBloodType());
        donation.setDonationDate(event.getDonationDate());
        donation.setStatus(event.getStatus());
        donation.setEventTimestamp(LocalDateTime.now());

        if (event.getLocationId() != null) {
            DonorLocation location = donorLocationRepository
                    .findTopByLocationIdOrderByEventTimestampDesc(event.getLocationId())
                    .orElse(null);
            donation.setLocation(location);
        }

        Donation savedDonation = donationRepository.save(donation);
        System.out.println("Created new donation history record: " + savedDonation.getDonationId());
    }

    @Transactional
    public void handleDonorLocationEvent(DonorLocationEvent event) {
        System.out.println("Processing DonorLocationEvent for locationId: " + event.getLocationId());

        Donor donor = donorRepository.findTopByDonorIdOrderByEventTimestampDesc(event.getDonorId())
                .orElse(null);

        if (donor == null) {
            System.out.println("Donor not found for location event, skipping");
            return;
        }

        DonorLocation location = new DonorLocation();
        location.setLocationId(event.getLocationId());
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
        location.setEventTimestamp(LocalDateTime.now());

        DonorLocation savedLocation = donorLocationRepository.save(location);
        System.out.println("Created new donor location history: " + savedLocation.getLocationId());

        var pending = donationEventBuffer.drain(location.getDonor().getDonorId(), location.getLocationId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }

    @Transactional
    public void handleReceiveRequestEvent(ReceiveRequestEvent event) {
        System.out.println("Processing ReceiveRequestEvent for requestId: " + event.getReceiveRequestId());

        Recipient recipient = recipientRepository.findTopByRecipientIdOrderByEventTimestampDesc(event.getRecipientId())
                .orElse(null);

        if (recipient == null) {
            System.out.println("Recipient not found, buffering receive request event: " + event.getRecipientId());
            receiveRequestEventBuffer.buffer(event.getRecipientId(), event.getLocationId(), () -> handleReceiveRequestEvent(event));
            return;
        }

        ReceiveRequest request = new ReceiveRequest();
        request.setReceiveRequestId(event.getReceiveRequestId());
        request.setRecipient(recipient);
        request.setRecipientId(recipient.getRecipientId());
        request.setRequestType(event.getRequestType());
        request.setRequestedBloodType(event.getRequestedBloodType());
        request.setRequestedOrgan(event.getRequestedOrgan());
        request.setRequestedTissue(event.getRequestedTissue());
        request.setRequestedStemCellType(event.getRequestedStemCellType());
        request.setUrgencyLevel(event.getUrgencyLevel());
        request.setQuantity(event.getQuantity());
        request.setRequestDate(event.getRequestDate());
        request.setStatus(event.getStatus());
        request.setNotes(event.getNotes());
        request.setEventTimestamp(LocalDateTime.now());

        if (event.getLocationId() != null) {
            RecipientLocation location = recipientLocationRepository
                    .findTopByLocationIdOrderByEventTimestampDesc(event.getLocationId())
                    .orElse(null);
            request.setLocation(location);
        }

        ReceiveRequest savedRequest = receiveRequestRepository.save(request);
        System.out.println("Created new receive request history record: " + savedRequest.getReceiveRequestId());
    }

    private Donation createDonationByType(DonationEvent event) {
        return switch (event.getDonationType()) {
            case BLOOD -> {
                BloodDonation bloodDonation = new BloodDonation();
                bloodDonation.setQuantity(event.getQuantity());
                yield bloodDonation;
            }
            case ORGAN -> {
                OrganDonation organDonation = new OrganDonation();
                organDonation.setOrganType(event.getOrganType());
                organDonation.setIsCompatible(event.getIsCompatible());
                organDonation.setOrganQuality(event.getOrganQuality());
                organDonation.setOrganViabilityExpiry(event.getOrganViabilityExpiry());
                organDonation.setColdIschemiaTime(event.getColdIschemiaTime());
                organDonation.setOrganPerfused(event.getOrganPerfused());
                organDonation.setOrganWeight(event.getOrganWeight());
                organDonation.setOrganSize(event.getOrganSize());
                organDonation.setFunctionalAssessment(event.getFunctionalAssessment());
                organDonation.setHasAbnormalities(event.getHasAbnormalities());
                organDonation.setAbnormalityDescription(event.getAbnormalityDescription());
                yield organDonation;
            }
            case TISSUE -> {
                TissueDonation tissueDonation = new TissueDonation();
                tissueDonation.setTissueType(event.getTissueType());
                tissueDonation.setQuantity(event.getQuantity());
                yield tissueDonation;
            }
            case STEM_CELL -> {
                StemCellDonation stemCellDonation = new StemCellDonation();
                stemCellDonation.setStemCellType(event.getStemCellType());
                stemCellDonation.setQuantity(event.getQuantity());
                yield stemCellDonation;
            }
        };
    }

    private void safeRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            System.err.println("Error processing buffered event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    public void handleRecipientEvent(RecipientEvent event) {
        System.out.println("Processing RecipientEvent for recipientId: " + event.getRecipientId());

        Recipient recipient = new Recipient();
        recipient.setRecipientId(event.getRecipientId());
        recipient.setUserId(event.getUserId());
        recipient.setAvailability(event.getAvailability());
        recipient.setEventTimestamp(LocalDateTime.now());

        if (event.getEligibilityCriteriaId() != null) {
            RecipientEligibilityCriteria eligibility = new RecipientEligibilityCriteria();
            eligibility.setEligibilityCriteriaId(event.getEligibilityCriteriaId());
            eligibility.setRecipient(recipient);
            eligibility.setAgeEligible(event.getAgeEligible());
            eligibility.setAge(event.getAge());
            eligibility.setDob(event.getDob());
            eligibility.setWeightEligible(event.getWeightEligible());
            eligibility.setWeight(event.getWeight());
            eligibility.setMedicallyEligible(event.getMedicallyEligible());
            eligibility.setLegalClearance(event.getLegalClearance());
            eligibility.setNotes(event.getEligibilityNotes());
            eligibility.setLastReviewed(event.getLastReviewed());
            eligibility.setHeight(event.getHeight());
            eligibility.setBodyMassIndex(event.getBodyMassIndex());
            eligibility.setBodySize(event.getBodySize());
            eligibility.setIsLivingDonor(event.getIsLivingDonor());
            eligibility.setSmokingStatus(event.getSmokingStatus());
            eligibility.setPackYears(event.getPackYears());
            eligibility.setQuitSmokingDate(event.getQuitSmokingDate());
            eligibility.setAlcoholStatus(event.getAlcoholStatus());
            eligibility.setDrinksPerWeek(event.getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(event.getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(event.getAlcoholAbstinenceMonths());
            recipient.setEligibilityCriteria(eligibility);
        }

        if (event.getMedicalDetailsId() != null) {
            RecipientMedicalDetails medicalDetails = new RecipientMedicalDetails();
            medicalDetails.setMedicalDetailsId(event.getMedicalDetailsId());
            medicalDetails.setRecipient(recipient);
            medicalDetails.setHemoglobinLevel(event.getHemoglobinLevel());
            medicalDetails.setBloodPressure(event.getBloodPressure());
            medicalDetails.setDiagnosis(event.getDiagnosis());
            medicalDetails.setAllergies(event.getAllergies());
            medicalDetails.setCurrentMedications(event.getCurrentMedications());
            medicalDetails.setAdditionalNotes(event.getAdditionalNotes());
            medicalDetails.setHasInfectiousDiseases(event.getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(event.getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(event.getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(event.getLiverFunctionTests());
            medicalDetails.setCardiacStatus(event.getCardiacStatus());
            medicalDetails.setPulmonaryFunction(event.getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(event.getOverallHealthStatus());
            recipient.setMedicalDetails(medicalDetails);
        }

        Recipient savedRecipient = recipientRepository.save(recipient);
        System.out.println("Created new recipient history snapshot: " + savedRecipient.getRecipientId()
                + " at timestamp: " + savedRecipient.getEventTimestamp());

        var pendingEvents = recipientEventBuffer.drain(recipient.getRecipientId());
        if (pendingEvents != null) {
            pendingEvents.forEach(this::safeRun);
        }
    }

    @Transactional
    public void handleRecipientLocationEvent(RecipientLocationEvent event) {
        System.out.println("Processing RecipientLocationEvent for locationId: " + event.getLocationId());

        Recipient recipient = recipientRepository.findTopByRecipientIdOrderByEventTimestampDesc(event.getRecipientId())
                .orElse(null);

        if (recipient == null) {
            System.out.println("Recipient not found for location event, skipping");
            return;
        }

        RecipientLocation location = new RecipientLocation();
        location.setLocationId(event.getLocationId());
        location.setRecipient(recipient);
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
        location.setEventTimestamp(LocalDateTime.now());

        RecipientLocation savedLocation = recipientLocationRepository.save(location);
        System.out.println("Created new recipient location history: " + savedLocation.getLocationId());

        var pending = receiveRequestEventBuffer.drain(location.getRecipient().getRecipientId(), location.getLocationId());
        if (pending != null) {
            pending.forEach(this::safeRun);
        }
    }

    @Transactional
    public void handleDonorHLAProfileEvent(DonorHLAProfileEvent event) {
        System.out.println("Processing DonorHLAProfileEvent for donorId: " + event.getDonorId());

        Donor donor = donorRepository.findTopByDonorIdOrderByEventTimestampDesc(event.getDonorId())
                .orElse(null);

        if (donor == null) {
            System.out.println("Donor not found for HLA profile event, skipping");
            return;
        }

        DonorHLAProfile hlaProfile = new DonorHLAProfile();
        hlaProfile.setId(event.getId());
        hlaProfile.setDonor(donor);
        hlaProfile.setHlaA1(event.getHlaA1());
        hlaProfile.setHlaA2(event.getHlaA2());
        hlaProfile.setHlaB1(event.getHlaB1());
        hlaProfile.setHlaB2(event.getHlaB2());
        hlaProfile.setHlaC1(event.getHlaC1());
        hlaProfile.setHlaC2(event.getHlaC2());
        hlaProfile.setHlaDR1(event.getHlaDR1());
        hlaProfile.setHlaDR2(event.getHlaDR2());
        hlaProfile.setHlaDQ1(event.getHlaDQ1());
        hlaProfile.setHlaDQ2(event.getHlaDQ2());
        hlaProfile.setHlaDP1(event.getHlaDP1());
        hlaProfile.setHlaDP2(event.getHlaDP2());
        hlaProfile.setTestingDate(event.getTestingDate());
        hlaProfile.setTestingMethod(event.getTestingMethod());
        hlaProfile.setLaboratoryName(event.getLaboratoryName());
        hlaProfile.setCertificationNumber(event.getCertificationNumber());
        hlaProfile.setHlaString(event.getHlaString());
        hlaProfile.setIsHighResolution(event.getIsHighResolution());
        hlaProfile.setEventTimestamp(LocalDateTime.now());

        DonorHLAProfile savedProfile = donorHLAProfileRepository.save(hlaProfile);
        System.out.println("Created new donor HLA profile history: " + savedProfile.getId());
    }

    @Transactional
    public void handleRecipientHLAProfileEvent(RecipientHLAProfileEvent event) {
        System.out.println("Processing RecipientHLAProfileEvent for recipientId: " + event.getRecipientId());

        Recipient recipient = recipientRepository.findTopByRecipientIdOrderByEventTimestampDesc(event.getRecipientId())
                .orElse(null);

        if (recipient == null) {
            System.out.println("Recipient not found for HLA profile event, skipping");
            return;
        }

        RecipientHLAProfile hlaProfile = new RecipientHLAProfile();
        hlaProfile.setId(event.getId());
        hlaProfile.setRecipient(recipient);
        hlaProfile.setHlaA1(event.getHlaA1());
        hlaProfile.setHlaA2(event.getHlaA2());
        hlaProfile.setHlaB1(event.getHlaB1());
        hlaProfile.setHlaB2(event.getHlaB2());
        hlaProfile.setHlaC1(event.getHlaC1());
        hlaProfile.setHlaC2(event.getHlaC2());
        hlaProfile.setHlaDR1(event.getHlaDR1());
        hlaProfile.setHlaDR2(event.getHlaDR2());
        hlaProfile.setHlaDQ1(event.getHlaDQ1());
        hlaProfile.setHlaDQ2(event.getHlaDQ2());
        hlaProfile.setHlaDP1(event.getHlaDP1());
        hlaProfile.setHlaDP2(event.getHlaDP2());
        hlaProfile.setTestingDate(event.getTestingDate());
        hlaProfile.setTestingMethod(event.getTestingMethod());
        hlaProfile.setLaboratoryName(event.getLaboratoryName());
        hlaProfile.setCertificationNumber(event.getCertificationNumber());
        hlaProfile.setHlaString(event.getHlaString());
        hlaProfile.setIsHighResolution(event.getIsHighResolution());
        hlaProfile.setEventTimestamp(LocalDateTime.now());

        RecipientHLAProfile savedProfile = recipientHLAProfileRepository.save(hlaProfile);
        System.out.println("Created new recipient HLA profile history: " + savedProfile.getId());
    }
}
