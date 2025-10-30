package com.matchingservice.service;

import com.matchingservice.enums.*;
import com.matchingservice.kafka.event.donor_events.*;
import com.matchingservice.kafka.event.recipient_events.*;
import com.matchingservice.model.MatchResult;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.*;
import com.matchingservice.repository.MatchResultRepository;
import com.matchingservice.repository.donor.*;
import com.matchingservice.repository.recipient.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class MatchingEventHandlerService {

    private final DonorRepository donorRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final DonationRepository donationRepository;
    private final DonorHLAProfileRepository donorHLAProfileRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientLocationRepository recipientLocationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final RecipientHLAProfileRepository recipientHLAProfileRepository;
    private final MatchResultRepository matchResultRepository;

    private final ConcurrentHashMap<UUID, ReentrantLock> donorLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> recipientLocks = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, DonorEventGroup> donorEventGroups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RecipientEventGroup> recipientEventGroups = new ConcurrentHashMap<>();

    @Transactional
    public void handleDonorEvent(DonorEvent event) {
        ReentrantLock lock = donorLocks.computeIfAbsent(event.getDonorId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing DonorEvent for donorId: " + event.getDonorId());

            DonorEventGroup group = donorEventGroups.computeIfAbsent(event.getDonorId(), k -> new DonorEventGroup());
            group.donorEvent = event;
            group.donorEventReceived = true;

            processCompleteDonorGroup(event.getDonorId(), group);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleDonationEvent(DonationEvent event) {
        ReentrantLock lock = donorLocks.computeIfAbsent(event.getDonorId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing DonationEvent for donationId: " + event.getDonationId());

            if (donationRepository.existsById(event.getDonationId())) {
                System.out.println("Donation already processed (Kafka retry), skipping: " + event.getDonationId());
                return;
            }

            DonorEventGroup group = donorEventGroups.computeIfAbsent(event.getDonorId(), k -> new DonorEventGroup());
            group.donationEvent = event;
            group.donationEventReceived = true;

            processCompleteDonorGroup(event.getDonorId(), group);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleDonorLocationEvent(DonorLocationEvent event) {
        ReentrantLock lock = donorLocks.computeIfAbsent(event.getDonorId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing DonorLocationEvent for locationId: " + event.getLocationId());

            DonorEventGroup group = donorEventGroups.computeIfAbsent(event.getDonorId(), k -> new DonorEventGroup());
            group.locationEvent = event;
            group.locationEventReceived = true;

            processCompleteDonorGroup(event.getDonorId(), group);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleDonorHLAProfileEvent(DonorHLAProfileEvent event) {
        ReentrantLock lock = donorLocks.computeIfAbsent(event.getDonorId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing DonorHLAProfileEvent for donorId: " + event.getDonorId());

            DonorEventGroup group = donorEventGroups.computeIfAbsent(event.getDonorId(), k -> new DonorEventGroup());
            group.hlaEvent = event;
            group.hlaEventReceived = true;

            processCompleteDonorGroup(event.getDonorId(), group);
        } finally {
            lock.unlock();
        }
    }

    private void processCompleteDonorGroup(UUID donorId, DonorEventGroup group) {
        if (!group.isComplete()) {
            System.out.println("Donor event group incomplete, waiting for more events. Status: " + group.getStatus());
            return;
        }

        if (group.processed) {
            System.out.println("Donor group already processed, skipping");
            return;
        }

        System.out.println("All 4 donor events received, processing group for donorId: " + donorId);

        Donor donor = new Donor();
        donor.setDonorId(group.donorEvent.getDonorId());
        donor.setUserId(group.donorEvent.getUserId());
        donor.setRegistrationDate(group.donorEvent.getRegistrationDate());
        donor.setStatus(DonorStatus.valueOf(group.donorEvent.getStatus()));
        donor.setEventTimestamp(LocalDateTime.now());

        if (group.donorEvent.getEligibilityCriteriaId() != null) {
            DonorEligibilityCriteria eligibility = new DonorEligibilityCriteria();
            eligibility.setEligibilityCriteriaId(group.donorEvent.getEligibilityCriteriaId());
            eligibility.setDonor(donor);
            eligibility.setWeight(group.donorEvent.getWeight());
            eligibility.setAge(group.donorEvent.getAge());
            eligibility.setDob(group.donorEvent.getDob());
            eligibility.setMedicalClearance(group.donorEvent.getMedicalClearance());
            eligibility.setRecentTattooOrPiercing(group.donorEvent.getRecentTattooOrPiercing());
            eligibility.setRecentTravelDetails(group.donorEvent.getRecentTravelDetails());
            eligibility.setRecentVaccination(group.donorEvent.getRecentVaccination());
            eligibility.setRecentSurgery(group.donorEvent.getRecentSurgery());
            eligibility.setChronicDiseases(group.donorEvent.getChronicDiseases());
            eligibility.setAllergies(group.donorEvent.getAllergies());
            eligibility.setLastDonationDate(group.donorEvent.getLastDonationDate());
            eligibility.setHeight(group.donorEvent.getHeight());
            eligibility.setBodyMassIndex(group.donorEvent.getBodyMassIndex());
            eligibility.setBodySize(group.donorEvent.getBodySize());
            eligibility.setIsLivingDonor(group.donorEvent.getIsLivingDonor());
            eligibility.setSmokingStatus(group.donorEvent.getSmokingStatus());
            eligibility.setPackYears(group.donorEvent.getPackYears());
            eligibility.setQuitSmokingDate(group.donorEvent.getQuitSmokingDate());
            eligibility.setAlcoholStatus(group.donorEvent.getAlcoholStatus());
            eligibility.setDrinksPerWeek(group.donorEvent.getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(group.donorEvent.getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(group.donorEvent.getAlcoholAbstinenceMonths());
            donor.setEligibilityCriteria(eligibility);
        }

        if (group.donorEvent.getMedicalDetailsId() != null) {
            DonorMedicalDetails medicalDetails = new DonorMedicalDetails();
            medicalDetails.setMedicalDetailsId(group.donorEvent.getMedicalDetailsId());
            medicalDetails.setDonor(donor);
            medicalDetails.setHemoglobinLevel(group.donorEvent.getHemoglobinLevel());
            medicalDetails.setBloodGlucoseLevel(group.donorEvent.getBloodGlucoseLevel());
            medicalDetails.setHasDiabetes(group.donorEvent.getHasDiabetes());
            medicalDetails.setBloodPressure(group.donorEvent.getBloodPressure());
            medicalDetails.setHasDiseases(group.donorEvent.getHasDiseases());
            medicalDetails.setTakingMedication(group.donorEvent.getTakingMedication());
            medicalDetails.setDiseaseDescription(group.donorEvent.getDiseaseDescription());
            medicalDetails.setCurrentMedications(group.donorEvent.getCurrentMedications());
            medicalDetails.setLastMedicalCheckup(group.donorEvent.getLastMedicalCheckup());
            medicalDetails.setMedicalHistory(group.donorEvent.getMedicalHistory());
            medicalDetails.setHasInfectiousDiseases(group.donorEvent.getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(group.donorEvent.getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(group.donorEvent.getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(group.donorEvent.getLiverFunctionTests());
            medicalDetails.setCardiacStatus(group.donorEvent.getCardiacStatus());
            medicalDetails.setPulmonaryFunction(group.donorEvent.getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(group.donorEvent.getOverallHealthStatus());
            donor.setMedicalDetails(medicalDetails);
        }

        Donor savedDonor = donorRepository.save(donor);
        System.out.println("Saved donor snapshot: " + savedDonor.getDonorId() + " with BP: " + savedDonor.getMedicalDetails().getBloodPressure());

        DonorLocation location = new DonorLocation();
        location.setLocationId(group.locationEvent.getLocationId());
        location.setDonor(savedDonor);
        location.setAddressLine(group.locationEvent.getAddressLine());
        location.setLandmark(group.locationEvent.getLandmark());
        location.setArea(group.locationEvent.getArea());
        location.setCity(group.locationEvent.getCity());
        location.setDistrict(group.locationEvent.getDistrict());
        location.setState(group.locationEvent.getState());
        location.setCountry(group.locationEvent.getCountry());
        location.setPincode(group.locationEvent.getPincode());
        location.setLatitude(group.locationEvent.getLatitude());
        location.setLongitude(group.locationEvent.getLongitude());
        location.setEventTimestamp(LocalDateTime.now());
        DonorLocation savedLocation = donorLocationRepository.save(location);
        System.out.println("Saved donor location: " + savedLocation.getLocationId());

        Donation donation = createDonationByType(group.donationEvent);
        donation.setDonationId(group.donationEvent.getDonationId());
        donation.setDonor(savedDonor);
        donation.setDonorId(savedDonor.getDonorId());
        donation.setUserId(savedDonor.getUserId());
        donation.setDonationType(group.donationEvent.getDonationType());
        donation.setBloodType(group.donationEvent.getBloodType());
        donation.setDonationDate(group.donationEvent.getDonationDate());
        donation.setStatus(group.donationEvent.getStatus());
        donation.setEventTimestamp(LocalDateTime.now());
        donation.setLocation(savedLocation);
        Donation savedDonation = donationRepository.save(donation);
        System.out.println("Saved donation: " + savedDonation.getDonationId() + " linked to donor with BP: " + savedDonor.getMedicalDetails().getBloodPressure());

        if (group.hlaEventReceived && group.hlaEvent != null) {
            DonorHLAProfile hlaProfile = new DonorHLAProfile();
            hlaProfile.setId(group.hlaEvent.getId());
            hlaProfile.setDonor(savedDonor);
            hlaProfile.setHlaA1(group.hlaEvent.getHlaA1());
            hlaProfile.setHlaA2(group.hlaEvent.getHlaA2());
            hlaProfile.setHlaB1(group.hlaEvent.getHlaB1());
            hlaProfile.setHlaB2(group.hlaEvent.getHlaB2());
            hlaProfile.setHlaC1(group.hlaEvent.getHlaC1());
            hlaProfile.setHlaC2(group.hlaEvent.getHlaC2());
            hlaProfile.setHlaDR1(group.hlaEvent.getHlaDR1());
            hlaProfile.setHlaDR2(group.hlaEvent.getHlaDR2());
            hlaProfile.setHlaDQ1(group.hlaEvent.getHlaDQ1());
            hlaProfile.setHlaDQ2(group.hlaEvent.getHlaDQ2());
            hlaProfile.setHlaDP1(group.hlaEvent.getHlaDP1());
            hlaProfile.setHlaDP2(group.hlaEvent.getHlaDP2());
            hlaProfile.setTestingDate(group.hlaEvent.getTestingDate());
            hlaProfile.setTestingMethod(group.hlaEvent.getTestingMethod());
            hlaProfile.setLaboratoryName(group.hlaEvent.getLaboratoryName());
            hlaProfile.setCertificationNumber(group.hlaEvent.getCertificationNumber());
            hlaProfile.setHlaString(group.hlaEvent.getHlaString());
            hlaProfile.setIsHighResolution(group.hlaEvent.getIsHighResolution());
            hlaProfile.setEventTimestamp(LocalDateTime.now());
            donorHLAProfileRepository.save(hlaProfile);
            System.out.println("Saved HLA profile: " + hlaProfile.getId());
        } else {
            System.out.println("No HLA profile event received (blood donation or not provided)");
        }
        group.processed = true;
        donorEventGroups.remove(donorId);

        System.out.println("✅ Complete donor group processed successfully for donation: " + savedDonation.getDonationId());
    }

    @Transactional
    public void handleRecipientEvent(RecipientEvent event) {
        ReentrantLock lock = recipientLocks.computeIfAbsent(event.getRecipientId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing RecipientEvent for recipientId: " + event.getRecipientId());

            RecipientEventGroup group = recipientEventGroups.computeIfAbsent(event.getRecipientId(), k -> new RecipientEventGroup());
            group.recipientEvent = event;
            group.recipientEventReceived = true;

            processCompleteRecipientGroup(event.getRecipientId(), group);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleReceiveRequestEvent(ReceiveRequestEvent event) {
        ReentrantLock lock = recipientLocks.computeIfAbsent(event.getRecipientId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing ReceiveRequestEvent for requestId: " + event.getReceiveRequestId());

            if (receiveRequestRepository.existsById(event.getReceiveRequestId())) {
                System.out.println("ReceiveRequest already processed (Kafka retry), skipping: " + event.getReceiveRequestId());
                return;
            }

            RecipientEventGroup group = recipientEventGroups.computeIfAbsent(event.getRecipientId(), k -> new RecipientEventGroup());
            group.receiveRequestEvent = event;
            group.receiveRequestEventReceived = true;

            processCompleteRecipientGroup(event.getRecipientId(), group);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleRecipientLocationEvent(RecipientLocationEvent event) {
        ReentrantLock lock = recipientLocks.computeIfAbsent(event.getRecipientId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing RecipientLocationEvent for locationId: " + event.getLocationId());

            RecipientEventGroup group = recipientEventGroups.computeIfAbsent(event.getRecipientId(), k -> new RecipientEventGroup());
            group.locationEvent = event;
            group.locationEventReceived = true;

            processCompleteRecipientGroup(event.getRecipientId(), group);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleRecipientHLAProfileEvent(RecipientHLAProfileEvent event) {
        ReentrantLock lock = recipientLocks.computeIfAbsent(event.getRecipientId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing RecipientHLAProfileEvent for recipientId: " + event.getRecipientId());

            RecipientEventGroup group = recipientEventGroups.computeIfAbsent(event.getRecipientId(), k -> new RecipientEventGroup());
            group.hlaEvent = event;
            group.hlaEventReceived = true;

            processCompleteRecipientGroup(event.getRecipientId(), group);
        } finally {
            lock.unlock();
        }
    }

    private void processCompleteRecipientGroup(UUID recipientId, RecipientEventGroup group) {
        if (!group.isComplete()) {
            System.out.println("Recipient event group incomplete, waiting for more events. Status: " + group.getStatus());
            return;
        }

        if (group.processed) {
            System.out.println("Recipient group already processed, skipping");
            return;
        }

        System.out.println("All 4 recipient events received, processing group for recipientId: " + recipientId);

        Recipient recipient = new Recipient();
        recipient.setRecipientId(group.recipientEvent.getRecipientId());
        recipient.setUserId(group.recipientEvent.getUserId());
        recipient.setAvailability(group.recipientEvent.getAvailability());
        recipient.setEventTimestamp(LocalDateTime.now());

        if (group.recipientEvent.getEligibilityCriteriaId() != null) {
            RecipientEligibilityCriteria eligibility = new RecipientEligibilityCriteria();
            eligibility.setEligibilityCriteriaId(group.recipientEvent.getEligibilityCriteriaId());
            eligibility.setRecipient(recipient);
            eligibility.setAgeEligible(group.recipientEvent.getAgeEligible());
            eligibility.setAge(group.recipientEvent.getAge());
            eligibility.setDob(group.recipientEvent.getDob());
            eligibility.setWeightEligible(group.recipientEvent.getWeightEligible());
            eligibility.setWeight(group.recipientEvent.getWeight());
            eligibility.setMedicallyEligible(group.recipientEvent.getMedicallyEligible());
            eligibility.setLegalClearance(group.recipientEvent.getLegalClearance());
            eligibility.setNotes(group.recipientEvent.getEligibilityNotes());
            eligibility.setLastReviewed(group.recipientEvent.getLastReviewed());
            eligibility.setHeight(group.recipientEvent.getHeight());
            eligibility.setBodyMassIndex(group.recipientEvent.getBodyMassIndex());
            eligibility.setBodySize(group.recipientEvent.getBodySize());
            eligibility.setIsLivingDonor(group.recipientEvent.getIsLivingDonor());
            eligibility.setSmokingStatus(group.recipientEvent.getSmokingStatus());
            eligibility.setPackYears(group.recipientEvent.getPackYears());
            eligibility.setQuitSmokingDate(group.recipientEvent.getQuitSmokingDate());
            eligibility.setAlcoholStatus(group.recipientEvent.getAlcoholStatus());
            eligibility.setDrinksPerWeek(group.recipientEvent.getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(group.recipientEvent.getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(group.recipientEvent.getAlcoholAbstinenceMonths());
            recipient.setEligibilityCriteria(eligibility);
        }

        if (group.recipientEvent.getMedicalDetailsId() != null) {
            RecipientMedicalDetails medicalDetails = new RecipientMedicalDetails();
            medicalDetails.setMedicalDetailsId(group.recipientEvent.getMedicalDetailsId());
            medicalDetails.setRecipient(recipient);
            medicalDetails.setHemoglobinLevel(group.recipientEvent.getHemoglobinLevel());
            medicalDetails.setBloodGlucoseLevel(group.recipientEvent.getBloodGlucoseLevel());
            medicalDetails.setHasDiabetes(group.recipientEvent.getHasDiabetes());
            medicalDetails.setBloodPressure(group.recipientEvent.getBloodPressure());
            medicalDetails.setDiagnosis(group.recipientEvent.getDiagnosis());
            medicalDetails.setAllergies(group.recipientEvent.getAllergies());
            medicalDetails.setCurrentMedications(group.recipientEvent.getCurrentMedications());
            medicalDetails.setAdditionalNotes(group.recipientEvent.getAdditionalNotes());
            medicalDetails.setHasInfectiousDiseases(group.recipientEvent.getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(group.recipientEvent.getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(group.recipientEvent.getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(group.recipientEvent.getLiverFunctionTests());
            medicalDetails.setCardiacStatus(group.recipientEvent.getCardiacStatus());
            medicalDetails.setPulmonaryFunction(group.recipientEvent.getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(group.recipientEvent.getOverallHealthStatus());
            recipient.setMedicalDetails(medicalDetails);
        }

        Recipient savedRecipient = recipientRepository.save(recipient);
        System.out.println("Saved recipient snapshot: " + savedRecipient.getRecipientId() + " with BP: " + savedRecipient.getMedicalDetails().getBloodPressure());

        RecipientLocation location = new RecipientLocation();
        location.setLocationId(group.locationEvent.getLocationId());
        location.setRecipient(savedRecipient);
        location.setAddressLine(group.locationEvent.getAddressLine());
        location.setLandmark(group.locationEvent.getLandmark());
        location.setArea(group.locationEvent.getArea());
        location.setCity(group.locationEvent.getCity());
        location.setDistrict(group.locationEvent.getDistrict());
        location.setState(group.locationEvent.getState());
        location.setCountry(group.locationEvent.getCountry());
        location.setPincode(group.locationEvent.getPincode());
        location.setLatitude(group.locationEvent.getLatitude());
        location.setLongitude(group.locationEvent.getLongitude());
        location.setEventTimestamp(LocalDateTime.now());
        RecipientLocation savedLocation = recipientLocationRepository.save(location);
        System.out.println("Saved recipient location: " + savedLocation.getLocationId());

        ReceiveRequest request = new ReceiveRequest();
        request.setReceiveRequestId(group.receiveRequestEvent.getReceiveRequestId());
        request.setRecipient(savedRecipient);
        request.setRecipientId(savedRecipient.getRecipientId());
        request.setUserId(savedRecipient.getUserId());
        request.setRequestType(group.receiveRequestEvent.getRequestType());
        request.setRequestedBloodType(group.receiveRequestEvent.getRequestedBloodType());
        request.setRequestedOrgan(group.receiveRequestEvent.getRequestedOrgan());
        request.setRequestedTissue(group.receiveRequestEvent.getRequestedTissue());
        request.setRequestedStemCellType(group.receiveRequestEvent.getRequestedStemCellType());
        request.setUrgencyLevel(group.receiveRequestEvent.getUrgencyLevel());
        request.setQuantity(group.receiveRequestEvent.getQuantity());
        request.setRequestDate(group.receiveRequestEvent.getRequestDate());
        request.setStatus(group.receiveRequestEvent.getStatus());
        request.setNotes(group.receiveRequestEvent.getNotes());
        request.setEventTimestamp(LocalDateTime.now());
        request.setLocation(savedLocation);
        ReceiveRequest savedRequest = receiveRequestRepository.save(request);
        System.out.println("Saved receive request: " + savedRequest.getReceiveRequestId() + " linked to recipient with BP: " + savedRecipient.getMedicalDetails().getBloodPressure());

        if (group.hlaEventReceived && group.hlaEvent != null) {
        RecipientHLAProfile hlaProfile = new RecipientHLAProfile();
        hlaProfile.setId(group.hlaEvent.getId());
        hlaProfile.setRecipient(savedRecipient);
        hlaProfile.setHlaA1(group.hlaEvent.getHlaA1());
        hlaProfile.setHlaA2(group.hlaEvent.getHlaA2());
        hlaProfile.setHlaB1(group.hlaEvent.getHlaB1());
        hlaProfile.setHlaB2(group.hlaEvent.getHlaB2());
        hlaProfile.setHlaC1(group.hlaEvent.getHlaC1());
        hlaProfile.setHlaC2(group.hlaEvent.getHlaC2());
        hlaProfile.setHlaDR1(group.hlaEvent.getHlaDR1());
        hlaProfile.setHlaDR2(group.hlaEvent.getHlaDR2());
        hlaProfile.setHlaDQ1(group.hlaEvent.getHlaDQ1());
        hlaProfile.setHlaDQ2(group.hlaEvent.getHlaDQ2());
        hlaProfile.setHlaDP1(group.hlaEvent.getHlaDP1());
        hlaProfile.setHlaDP2(group.hlaEvent.getHlaDP2());
        hlaProfile.setTestingDate(group.hlaEvent.getTestingDate());
        hlaProfile.setTestingMethod(group.hlaEvent.getTestingMethod());
        hlaProfile.setLaboratoryName(group.hlaEvent.getLaboratoryName());
        hlaProfile.setCertificationNumber(group.hlaEvent.getCertificationNumber());
        hlaProfile.setHlaString(group.hlaEvent.getHlaString());
        hlaProfile.setIsHighResolution(group.hlaEvent.getIsHighResolution());
        hlaProfile.setEventTimestamp(LocalDateTime.now());
        recipientHLAProfileRepository.save(hlaProfile);
        System.out.println("Saved HLA profile: " + hlaProfile.getId());
        } else {
            System.out.println("No HLA profile event received (blood request or not provided)");
        }

        group.processed = true;
        recipientEventGroups.remove(recipientId);

        System.out.println("✅ Complete recipient group processed successfully for request: " + savedRequest.getReceiveRequestId());
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

    private static class DonorEventGroup {
        DonorEvent donorEvent;
        DonationEvent donationEvent;
        DonorLocationEvent locationEvent;
        DonorHLAProfileEvent hlaEvent;

        boolean donorEventReceived = false;
        boolean donationEventReceived = false;
        boolean locationEventReceived = false;
        boolean hlaEventReceived = false;
        boolean processed = false;

        boolean isComplete() {
            if (!donorEventReceived || !donationEventReceived || !locationEventReceived) {
                return false;
            }

            if (donationEvent != null && donationEvent.getDonationType() == DonationType.BLOOD) {
                return true;
            }

            return hlaEventReceived;
        }

        String getStatus() {
            String hlaRequired = (donationEvent != null && donationEvent.getDonationType() == DonationType.BLOOD)
                    ? "(optional)" : "(required)";
            return String.format("Donor:%s Donation:%s Location:%s HLA:%s%s",
                    donorEventReceived, donationEventReceived, locationEventReceived,
                    hlaEventReceived, hlaRequired);
        }
    }


    private static class RecipientEventGroup {
        RecipientEvent recipientEvent;
        ReceiveRequestEvent receiveRequestEvent;
        RecipientLocationEvent locationEvent;
        RecipientHLAProfileEvent hlaEvent;

        boolean recipientEventReceived = false;
        boolean receiveRequestEventReceived = false;
        boolean locationEventReceived = false;
        boolean hlaEventReceived = false;
        boolean processed = false;

        boolean isComplete() {
            if (!recipientEventReceived || !receiveRequestEventReceived || !locationEventReceived) {
                return false;
            }
            if (receiveRequestEvent != null && receiveRequestEvent.getRequestType() == RequestType.BLOOD) {
                return true;
            }
            return hlaEventReceived;
        }

        String getStatus() {
            String hlaRequired = (receiveRequestEvent != null && receiveRequestEvent.getRequestType() == RequestType.BLOOD)
                    ? "(optional)" : "(required)";
            return String.format("Recipient:%s Request:%s Location:%s HLA:%s%s",
                    recipientEventReceived, receiveRequestEventReceived, locationEventReceived,
                    hlaEventReceived, hlaRequired);
        }
    }

    @Transactional
    public void handleDonationCancelledEvent(DonationCancelledEvent event) {
        System.out.println("========================================");
        System.out.println("Processing Donation Cancellation");
        System.out.println("Donation ID: " + event.getDonationId());
        System.out.println("Donor User ID: " + event.getDonorUserId());
        System.out.println("Reason: " + event.getCancellationReason());
        System.out.println("========================================");

        try {
            donationRepository.findById(event.getDonationId())
                    .ifPresent(donation -> {
                        donation.setStatus(DonationStatus.CANCELLED_BY_DONOR);
                        donationRepository.save(donation);
                        System.out.println("✓ Updated donation " + donation.getDonationId() +
                                " status to CANCELLED_BY_DONOR in matching-service");
                    });

            List<MatchStatus> activeStatuses = Arrays.asList(
                    MatchStatus.PENDING,
                    MatchStatus.DONOR_CONFIRMED,
                    MatchStatus.RECIPIENT_CONFIRMED
            );

            List<MatchResult> activeMatches = matchResultRepository
                    .findByDonationIdAndStatusIn(event.getDonationId(), activeStatuses);

            if (activeMatches.isEmpty()) {
                System.out.println("No active matches found for donation " + event.getDonationId());
                return;
            }

            System.out.println("Found " + activeMatches.size() + " active matches to expire");

            int expiredCount = 0;
            for (MatchResult match : activeMatches) {
                try {
                    match.setStatus(MatchStatus.CANCELLED_BY_DONOR);
                    match.setExpiryReason("DONATION_CANCELLED_BY_DONOR: " +
                            event.getCancellationReason());
                    match.setExpiredAt(LocalDateTime.now());
                    matchResultRepository.save(match);
                    expiredCount++;
                    System.out.println("Expired match " + match.getId() +
                            " | Donation: " + match.getDonationId() +
                            " - Request: " + match.getReceiveRequestId());
                } catch (Exception e) {
                    System.err.println("Failed to expire match " + match.getId() +
                            ": " + e.getMessage());
                }
            }

            System.out.println("Successfully expired " + expiredCount + " out of " +
                    activeMatches.size() + " matches for donation " +
                    event.getDonationId());
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error handling donation cancellation event: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void handleRequestCancelledEvent(RequestCancelledEvent event) {
        System.out.println("========================================");
        System.out.println("Processing Request Cancellation");
        System.out.println("Request ID: " + event.getRequestId());
        System.out.println("Recipient User ID: " + event.getRecipientUserId());
        System.out.println("Reason: " + event.getCancellationReason());
        System.out.println("========================================");

        try {
            receiveRequestRepository.findById(event.getRequestId())
                    .ifPresent(request -> {
                        request.setStatus(RequestStatus.CANCELLED_BY_RECIPIENT);
                        receiveRequestRepository.save(request);
                        System.out.println("✓ Updated request " + request.getReceiveRequestId() +
                                " status to CANCELLED_BY_RECIPIENT in matching-service");
                    });

            List<MatchStatus> activeStatuses = Arrays.asList(
                    MatchStatus.PENDING,
                    MatchStatus.DONOR_CONFIRMED,
                    MatchStatus.RECIPIENT_CONFIRMED
            );

            List<MatchResult> activeMatches = matchResultRepository
                    .findByReceiveRequestIdAndStatusIn(event.getRequestId(), activeStatuses);

            if (activeMatches.isEmpty()) {
                System.out.println("No active matches found for request " + event.getRequestId());
                return;
            }

            System.out.println("Found " + activeMatches.size() + " active matches to expire");

            int expiredCount = 0;
            for (MatchResult match : activeMatches) {
                try {
                    match.setStatus(MatchStatus.CANCELLED_BY_RECIPIENT);
                    match.setExpiryReason("REQUEST_CANCELLED_BY_RECIPIENT: " +
                            event.getCancellationReason());
                    match.setExpiredAt(LocalDateTime.now());
                    matchResultRepository.save(match);
                    expiredCount++;
                    System.out.println("Expired match " + match.getId() +
                            " | Donation: " + match.getDonationId() +
                            " - Request: " + match.getReceiveRequestId());
                } catch (Exception e) {
                    System.err.println("Failed to expire match " + match.getId() +
                            ": " + e.getMessage());
                }
            }

            System.out.println("Successfully expired " + expiredCount + " out of " +
                    activeMatches.size() + " matches for request " +
                    event.getRequestId());
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error handling request cancellation event: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


}
