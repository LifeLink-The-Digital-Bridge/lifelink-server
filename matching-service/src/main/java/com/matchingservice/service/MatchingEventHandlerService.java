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
import java.util.UUID;
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
    private final DonorEventBuffer donorEventBuffer;
    private final DonationEventBuffer donationEventBuffer;
    private final RecipientEventBuffer recipientEventBuffer;
    private final ReceiveRequestEventBuffer receiveRequestEventBuffer;

    private final ConcurrentHashMap<UUID, ReentrantLock> donorLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ReentrantLock> recipientLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> donorProcessed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> recipientProcessed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> donationCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> receiveRequestCounts = new ConcurrentHashMap<>();

    @Transactional
    public void handleDonorEvent(DonorEvent event) {
        ReentrantLock lock = donorLocks.computeIfAbsent(event.getDonorId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing DonorEvent for donorId: " + event.getDonorId());

            if (donorProcessed.containsKey(event.getDonorId())) {
                System.out.println("Donor already processed, skipping");
                return;
            }

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
            donorRepository.flush();
            donorProcessed.put(event.getDonorId(), true);
            donationCounts.put(event.getDonorId(), 0);
            System.out.println("Created new donor history snapshot: " + savedDonor.getDonorId() + " at timestamp: " + savedDonor.getEventTimestamp());

            drainAllDonorEvents(savedDonor.getDonorId());
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

            if (!donorProcessed.containsKey(event.getDonorId())) {
                System.out.println("Donor not processed yet, buffering donation event: " + event.getDonorId());
                donationEventBuffer.buffer(event.getDonorId(), event.getLocationId(), () -> handleDonationEvent(event));
                return;
            }

            Donor donor = findLatestDonor(event.getDonorId());
            if (donor == null) {
                System.out.println("Donor not found, buffering donation event: " + event.getDonorId());
                donationEventBuffer.buffer(event.getDonorId(), event.getLocationId(), () -> handleDonationEvent(event));
                return;
            }

            DonorLocation donorLocation = null;
            if (event.getLocationId() != null) {
                donorLocation = donorLocationRepository.findTopByLocationIdOrderByEventTimestampDesc(event.getLocationId()).orElse(null);
                if (donorLocation == null) {
                    System.out.println("Location not found, buffering donation event");
                    donationEventBuffer.buffer(event.getDonorId(), event.getLocationId(), () -> handleDonationEvent(event));
                    return;
                }
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

            if (donorLocation != null) {
                donation.setLocation(donorLocation);
            }

            Donation savedDonation = donationRepository.save(donation);
            System.out.println("Created new donation history record: " + savedDonation.getDonationId());

            int currentDonationCount = donationCounts.getOrDefault(event.getDonorId(), 0);
            currentDonationCount++;
            donationCounts.put(event.getDonorId(), currentDonationCount);

            if (currentDonationCount > 1) {
                System.out.println("Creating new donor snapshot for subsequent donation #" + currentDonationCount);
                createNewDonorSnapshotForDonation(donor, event.getDonationDate(), donorLocation);
            } else {
                System.out.println("First donation for this donor, not creating additional snapshot");
            }
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

            if (!donorProcessed.containsKey(event.getDonorId())) {
                System.out.println("Donor not processed yet, buffering location event");
                donorEventBuffer.buffer(event.getDonorId(), () -> handleDonorLocationEvent(event));
                return;
            }

            Donor donor = findLatestDonor(event.getDonorId());
            if (donor == null) {
                System.out.println("Donor not found for location event, buffering");
                donorEventBuffer.buffer(event.getDonorId(), () -> handleDonorLocationEvent(event));
                return;
            }

            DonorLocation existingLocation = donorLocationRepository.findTopByLocationIdOrderByEventTimestampDesc(event.getLocationId()).orElse(null);
            if (existingLocation != null) {
                System.out.println("Location already exists, skipping: " + event.getLocationId());
                drainDonationEventsForLocation(event.getDonorId(), event.getLocationId());
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

            drainDonationEventsForLocation(event.getDonorId(), event.getLocationId());
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

            if (!donorProcessed.containsKey(event.getDonorId())) {
                System.out.println("Donor not processed yet, buffering HLA profile event");
                donorEventBuffer.buffer(event.getDonorId(), () -> handleDonorHLAProfileEvent(event));
                return;
            }

            Donor donor = findLatestDonor(event.getDonorId());
            if (donor == null) {
                System.out.println("Donor not found for HLA profile event, buffering");
                donorEventBuffer.buffer(event.getDonorId(), () -> handleDonorHLAProfileEvent(event));
                return;
            }

            DonorHLAProfile existingProfile = donorHLAProfileRepository.findTopByDonor_DonorIdAndIdOrderByEventTimestampDesc(event.getDonorId(), event.getId()).orElse(null);
            if (existingProfile != null) {
                System.out.println("HLA profile already exists for this donor and ID, skipping: " + event.getId());
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
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void handleRecipientEvent(RecipientEvent event) {
        ReentrantLock lock = recipientLocks.computeIfAbsent(event.getRecipientId(), k -> new ReentrantLock());
        lock.lock();
        try {
            System.out.println("Processing RecipientEvent for recipientId: " + event.getRecipientId());

            if (recipientProcessed.containsKey(event.getRecipientId())) {
                System.out.println("Recipient already processed, skipping");
                return;
            }

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
            recipientRepository.flush();
            recipientProcessed.put(event.getRecipientId(), true);
            receiveRequestCounts.put(event.getRecipientId(), 0);
            System.out.println("Created new recipient history snapshot: " + savedRecipient.getRecipientId() + " at timestamp: " + savedRecipient.getEventTimestamp());

            drainAllRecipientEvents(savedRecipient.getRecipientId());
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

            if (!recipientProcessed.containsKey(event.getRecipientId())) {
                System.out.println("Recipient not processed yet, buffering location event");
                recipientEventBuffer.buffer(event.getRecipientId(), () -> handleRecipientLocationEvent(event));
                return;
            }

            Recipient recipient = findLatestRecipient(event.getRecipientId());
            if (recipient == null) {
                System.out.println("Recipient not found for location event, buffering");
                recipientEventBuffer.buffer(event.getRecipientId(), () -> handleRecipientLocationEvent(event));
                return;
            }

            RecipientLocation existingLocation = recipientLocationRepository.findTopByLocationIdOrderByEventTimestampDesc(event.getLocationId()).orElse(null);
            if (existingLocation != null) {
                System.out.println("Location already exists, skipping: " + event.getLocationId());
                drainReceiveRequestEventsForLocation(event.getRecipientId(), event.getLocationId());
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

            drainReceiveRequestEventsForLocation(event.getRecipientId(), event.getLocationId());
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

            if (!recipientProcessed.containsKey(event.getRecipientId())) {
                System.out.println("Recipient not processed yet, buffering HLA profile event");
                recipientEventBuffer.buffer(event.getRecipientId(), () -> handleRecipientHLAProfileEvent(event));
                return;
            }

            Recipient recipient = findLatestRecipient(event.getRecipientId());
            if (recipient == null) {
                System.out.println("Recipient not found for HLA profile event, buffering");
                recipientEventBuffer.buffer(event.getRecipientId(), () -> handleRecipientHLAProfileEvent(event));
                return;
            }

            RecipientHLAProfile existingProfile = recipientHLAProfileRepository.findTopByRecipient_RecipientIdAndIdOrderByEventTimestampDesc(event.getRecipientId(), event.getId()).orElse(null);
            if (existingProfile != null) {
                System.out.println("HLA profile already exists for this recipient and ID, skipping: " + event.getId());
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

            if (!recipientProcessed.containsKey(event.getRecipientId())) {
                System.out.println("Recipient not processed yet, buffering receive request event: " + event.getRecipientId());
                receiveRequestEventBuffer.buffer(event.getRecipientId(), event.getLocationId(), () -> handleReceiveRequestEvent(event));
                return;
            }

            Recipient recipient = findLatestRecipient(event.getRecipientId());
            if (recipient == null) {
                System.out.println("Recipient not found, buffering receive request event: " + event.getRecipientId());
                receiveRequestEventBuffer.buffer(event.getRecipientId(), event.getLocationId(), () -> handleReceiveRequestEvent(event));
                return;
            }

            RecipientLocation recipientLocation = null;
            if (event.getLocationId() != null) {
                recipientLocation = recipientLocationRepository.findTopByLocationIdOrderByEventTimestampDesc(event.getLocationId()).orElse(null);
                if (recipientLocation == null) {
                    System.out.println("Location not found, buffering receive request event");
                    receiveRequestEventBuffer.buffer(event.getRecipientId(), event.getLocationId(), () -> handleReceiveRequestEvent(event));
                    return;
                }
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

            if (recipientLocation != null) {
                request.setLocation(recipientLocation);
            }

            ReceiveRequest savedRequest = receiveRequestRepository.save(request);
            System.out.println("Created new receive request history record: " + savedRequest.getReceiveRequestId());

            int currentRequestCount = receiveRequestCounts.getOrDefault(event.getRecipientId(), 0);
            currentRequestCount++;
            receiveRequestCounts.put(event.getRecipientId(), currentRequestCount);

            if (currentRequestCount > 1) {
                System.out.println("Creating new recipient snapshot for subsequent request #" + currentRequestCount);
                createNewRecipientSnapshotForRequest(recipient, event.getRequestDate(), recipientLocation);
            } else {
                System.out.println("First request for this recipient, not creating additional snapshot");
            }
        } finally {
            lock.unlock();
        }
    }

    private Donor findLatestDonor(UUID donorId) {
        return donorRepository.findTopByDonorIdOrderByEventTimestampDesc(donorId).orElse(null);
    }

    private Recipient findLatestRecipient(UUID recipientId) {
        return recipientRepository.findTopByRecipientIdOrderByEventTimestampDesc(recipientId).orElse(null);
    }

    private void drainAllDonorEvents(UUID donorId) {
        var pendingDonorEvents = donorEventBuffer.drain(donorId);
        if (pendingDonorEvents != null && !pendingDonorEvents.isEmpty()) {
            System.out.println("Processing " + pendingDonorEvents.size() + " buffered donor events");
            pendingDonorEvents.forEach(this::safeRun);
        }
    }

    private void drainAllRecipientEvents(UUID recipientId) {
        var pendingEvents = recipientEventBuffer.drain(recipientId);
        if (pendingEvents != null && !pendingEvents.isEmpty()) {
            System.out.println("Processing " + pendingEvents.size() + " buffered recipient events");
            pendingEvents.forEach(this::safeRun);
        }
    }

    private void drainDonationEventsForLocation(UUID donorId, UUID locationId) {
        var pending = donationEventBuffer.drain(donorId, locationId);
        if (pending != null && !pending.isEmpty()) {
            System.out.println("Processing " + pending.size() + " buffered donation events for location");
            pending.forEach(this::safeRun);
        }
    }

    private void drainReceiveRequestEventsForLocation(UUID recipientId, UUID locationId) {
        var pending = receiveRequestEventBuffer.drain(recipientId, locationId);
        if (pending != null && !pending.isEmpty()) {
            System.out.println("Processing " + pending.size() + " buffered receive request events for location");
            pending.forEach(this::safeRun);
        }
    }

    private void createNewDonorSnapshotForDonation(Donor donor, java.time.LocalDate donationDate, DonorLocation location) {
        Donor newDonor = new Donor();
        newDonor.setDonorId(donor.getDonorId());
        newDonor.setUserId(donor.getUserId());
        newDonor.setRegistrationDate(donor.getRegistrationDate());
        newDonor.setStatus(donor.getStatus());
        newDonor.setEventTimestamp(LocalDateTime.now());

        if (donor.getEligibilityCriteria() != null) {
            DonorEligibilityCriteria eligibility = new DonorEligibilityCriteria();
            eligibility.setEligibilityCriteriaId(donor.getEligibilityCriteria().getEligibilityCriteriaId());
            eligibility.setDonor(newDonor);
            eligibility.setWeight(donor.getEligibilityCriteria().getWeight());
            eligibility.setAge(donor.getEligibilityCriteria().getAge());
            eligibility.setDob(donor.getEligibilityCriteria().getDob());
            eligibility.setMedicalClearance(donor.getEligibilityCriteria().getMedicalClearance());
            eligibility.setRecentTattooOrPiercing(donor.getEligibilityCriteria().getRecentTattooOrPiercing());
            eligibility.setRecentTravelDetails(donor.getEligibilityCriteria().getRecentTravelDetails());
            eligibility.setRecentVaccination(donor.getEligibilityCriteria().getRecentVaccination());
            eligibility.setRecentSurgery(donor.getEligibilityCriteria().getRecentSurgery());
            eligibility.setChronicDiseases(donor.getEligibilityCriteria().getChronicDiseases());
            eligibility.setAllergies(donor.getEligibilityCriteria().getAllergies());
            eligibility.setLastDonationDate(donationDate);
            eligibility.setHeight(donor.getEligibilityCriteria().getHeight());
            eligibility.setBodyMassIndex(donor.getEligibilityCriteria().getBodyMassIndex());
            eligibility.setBodySize(donor.getEligibilityCriteria().getBodySize());
            eligibility.setIsLivingDonor(donor.getEligibilityCriteria().getIsLivingDonor());
            eligibility.setSmokingStatus(donor.getEligibilityCriteria().getSmokingStatus());
            eligibility.setPackYears(donor.getEligibilityCriteria().getPackYears());
            eligibility.setQuitSmokingDate(donor.getEligibilityCriteria().getQuitSmokingDate());
            eligibility.setAlcoholStatus(donor.getEligibilityCriteria().getAlcoholStatus());
            eligibility.setDrinksPerWeek(donor.getEligibilityCriteria().getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(donor.getEligibilityCriteria().getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(donor.getEligibilityCriteria().getAlcoholAbstinenceMonths());
            newDonor.setEligibilityCriteria(eligibility);
        }

        if (donor.getMedicalDetails() != null) {
            DonorMedicalDetails medicalDetails = new DonorMedicalDetails();
            medicalDetails.setMedicalDetailsId(donor.getMedicalDetails().getMedicalDetailsId());
            medicalDetails.setDonor(newDonor);
            medicalDetails.setHemoglobinLevel(donor.getMedicalDetails().getHemoglobinLevel());
            medicalDetails.setBloodPressure(donor.getMedicalDetails().getBloodPressure());
            medicalDetails.setHasDiseases(donor.getMedicalDetails().getHasDiseases());
            medicalDetails.setTakingMedication(donor.getMedicalDetails().getTakingMedication());
            medicalDetails.setDiseaseDescription(donor.getMedicalDetails().getDiseaseDescription());
            medicalDetails.setCurrentMedications(donor.getMedicalDetails().getCurrentMedications());
            medicalDetails.setLastMedicalCheckup(donor.getMedicalDetails().getLastMedicalCheckup());
            medicalDetails.setMedicalHistory(donor.getMedicalDetails().getMedicalHistory());
            medicalDetails.setHasInfectiousDiseases(donor.getMedicalDetails().getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(donor.getMedicalDetails().getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(donor.getMedicalDetails().getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(donor.getMedicalDetails().getLiverFunctionTests());
            medicalDetails.setCardiacStatus(donor.getMedicalDetails().getCardiacStatus());
            medicalDetails.setPulmonaryFunction(donor.getMedicalDetails().getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(donor.getMedicalDetails().getOverallHealthStatus());
            newDonor.setMedicalDetails(medicalDetails);
        }

        donorRepository.save(newDonor);

        if (location != null) {
            DonorLocation newLocation = new DonorLocation();
            newLocation.setLocationId(location.getLocationId());
            newLocation.setDonor(newDonor);
            newLocation.setAddressLine(location.getAddressLine());
            newLocation.setLandmark(location.getLandmark());
            newLocation.setArea(location.getArea());
            newLocation.setCity(location.getCity());
            newLocation.setDistrict(location.getDistrict());
            newLocation.setState(location.getState());
            newLocation.setCountry(location.getCountry());
            newLocation.setPincode(location.getPincode());
            newLocation.setLatitude(location.getLatitude());
            newLocation.setLongitude(location.getLongitude());
            newLocation.setEventTimestamp(LocalDateTime.now());
            donorLocationRepository.save(newLocation);
        }

        DonorHLAProfile latestHLA = donorHLAProfileRepository.findTopByDonor_DonorIdOrderByEventTimestampDesc(donor.getDonorId()).orElse(null);
        if (latestHLA != null) {
            DonorHLAProfile newHLA = new DonorHLAProfile();
            newHLA.setId(latestHLA.getId());
            newHLA.setDonor(newDonor);
            newHLA.setHlaA1(latestHLA.getHlaA1());
            newHLA.setHlaA2(latestHLA.getHlaA2());
            newHLA.setHlaB1(latestHLA.getHlaB1());
            newHLA.setHlaB2(latestHLA.getHlaB2());
            newHLA.setHlaC1(latestHLA.getHlaC1());
            newHLA.setHlaC2(latestHLA.getHlaC2());
            newHLA.setHlaDR1(latestHLA.getHlaDR1());
            newHLA.setHlaDR2(latestHLA.getHlaDR2());
            newHLA.setHlaDQ1(latestHLA.getHlaDQ1());
            newHLA.setHlaDQ2(latestHLA.getHlaDQ2());
            newHLA.setHlaDP1(latestHLA.getHlaDP1());
            newHLA.setHlaDP2(latestHLA.getHlaDP2());
            newHLA.setTestingDate(latestHLA.getTestingDate());
            newHLA.setTestingMethod(latestHLA.getTestingMethod());
            newHLA.setLaboratoryName(latestHLA.getLaboratoryName());
            newHLA.setCertificationNumber(latestHLA.getCertificationNumber());
            newHLA.setHlaString(latestHLA.getHlaString());
            newHLA.setIsHighResolution(latestHLA.getIsHighResolution());
            newHLA.setEventTimestamp(LocalDateTime.now());
            donorHLAProfileRepository.save(newHLA);
        }
    }

    private void createNewRecipientSnapshotForRequest(Recipient recipient, java.time.LocalDate requestDate, RecipientLocation location) {
        Recipient newRecipient = new Recipient();
        newRecipient.setRecipientId(recipient.getRecipientId());
        newRecipient.setUserId(recipient.getUserId());
        newRecipient.setAvailability(recipient.getAvailability());
        newRecipient.setEventTimestamp(LocalDateTime.now());

        if (recipient.getEligibilityCriteria() != null) {
            RecipientEligibilityCriteria eligibility = new RecipientEligibilityCriteria();
            eligibility.setEligibilityCriteriaId(recipient.getEligibilityCriteria().getEligibilityCriteriaId());
            eligibility.setRecipient(newRecipient);
            eligibility.setAgeEligible(recipient.getEligibilityCriteria().getAgeEligible());
            eligibility.setAge(recipient.getEligibilityCriteria().getAge());
            eligibility.setDob(recipient.getEligibilityCriteria().getDob());
            eligibility.setWeightEligible(recipient.getEligibilityCriteria().getWeightEligible());
            eligibility.setWeight(recipient.getEligibilityCriteria().getWeight());
            eligibility.setMedicallyEligible(recipient.getEligibilityCriteria().getMedicallyEligible());
            eligibility.setLegalClearance(recipient.getEligibilityCriteria().getLegalClearance());
            eligibility.setNotes(recipient.getEligibilityCriteria().getNotes());
            eligibility.setLastReviewed(requestDate);
            eligibility.setHeight(recipient.getEligibilityCriteria().getHeight());
            eligibility.setBodyMassIndex(recipient.getEligibilityCriteria().getBodyMassIndex());
            eligibility.setBodySize(recipient.getEligibilityCriteria().getBodySize());
            eligibility.setIsLivingDonor(recipient.getEligibilityCriteria().getIsLivingDonor());
            eligibility.setSmokingStatus(recipient.getEligibilityCriteria().getSmokingStatus());
            eligibility.setPackYears(recipient.getEligibilityCriteria().getPackYears());
            eligibility.setQuitSmokingDate(recipient.getEligibilityCriteria().getQuitSmokingDate());
            eligibility.setAlcoholStatus(recipient.getEligibilityCriteria().getAlcoholStatus());
            eligibility.setDrinksPerWeek(recipient.getEligibilityCriteria().getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(recipient.getEligibilityCriteria().getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(recipient.getEligibilityCriteria().getAlcoholAbstinenceMonths());
            newRecipient.setEligibilityCriteria(eligibility);
        }

        if (recipient.getMedicalDetails() != null) {
            RecipientMedicalDetails medicalDetails = new RecipientMedicalDetails();
            medicalDetails.setMedicalDetailsId(recipient.getMedicalDetails().getMedicalDetailsId());
            medicalDetails.setRecipient(newRecipient);
            medicalDetails.setHemoglobinLevel(recipient.getMedicalDetails().getHemoglobinLevel());
            medicalDetails.setBloodPressure(recipient.getMedicalDetails().getBloodPressure());
            medicalDetails.setDiagnosis(recipient.getMedicalDetails().getDiagnosis());
            medicalDetails.setAllergies(recipient.getMedicalDetails().getAllergies());
            medicalDetails.setCurrentMedications(recipient.getMedicalDetails().getCurrentMedications());
            medicalDetails.setAdditionalNotes(recipient.getMedicalDetails().getAdditionalNotes());
            medicalDetails.setHasInfectiousDiseases(recipient.getMedicalDetails().getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(recipient.getMedicalDetails().getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(recipient.getMedicalDetails().getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(recipient.getMedicalDetails().getLiverFunctionTests());
            medicalDetails.setCardiacStatus(recipient.getMedicalDetails().getCardiacStatus());
            medicalDetails.setPulmonaryFunction(recipient.getMedicalDetails().getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(recipient.getMedicalDetails().getOverallHealthStatus());
            newRecipient.setMedicalDetails(medicalDetails);
        }

        recipientRepository.save(newRecipient);

        if (location != null) {
            RecipientLocation newLocation = new RecipientLocation();
            newLocation.setLocationId(location.getLocationId());
            newLocation.setRecipient(newRecipient);
            newLocation.setAddressLine(location.getAddressLine());
            newLocation.setLandmark(location.getLandmark());
            newLocation.setArea(location.getArea());
            newLocation.setCity(location.getCity());
            newLocation.setDistrict(location.getDistrict());
            newLocation.setState(location.getState());
            newLocation.setCountry(location.getCountry());
            newLocation.setPincode(location.getPincode());
            newLocation.setLatitude(location.getLatitude());
            newLocation.setLongitude(location.getLongitude());
            newLocation.setEventTimestamp(LocalDateTime.now());
            recipientLocationRepository.save(newLocation);
        }

        RecipientHLAProfile latestHLA = recipientHLAProfileRepository.findTopByRecipient_RecipientIdOrderByEventTimestampDesc(recipient.getRecipientId()).orElse(null);
        if (latestHLA != null) {
            RecipientHLAProfile newHLA = new RecipientHLAProfile();
            newHLA.setId(latestHLA.getId());
            newHLA.setRecipient(newRecipient);
            newHLA.setHlaA1(latestHLA.getHlaA1());
            newHLA.setHlaA2(latestHLA.getHlaA2());
            newHLA.setHlaB1(latestHLA.getHlaB1());
            newHLA.setHlaB2(latestHLA.getHlaB2());
            newHLA.setHlaC1(latestHLA.getHlaC1());
            newHLA.setHlaC2(latestHLA.getHlaC2());
            newHLA.setHlaDR1(latestHLA.getHlaDR1());
            newHLA.setHlaDR2(latestHLA.getHlaDR2());
            newHLA.setHlaDQ1(latestHLA.getHlaDQ1());
            newHLA.setHlaDQ2(latestHLA.getHlaDQ2());
            newHLA.setHlaDP1(latestHLA.getHlaDP1());
            newHLA.setHlaDP2(latestHLA.getHlaDP2());
            newHLA.setTestingDate(latestHLA.getTestingDate());
            newHLA.setTestingMethod(latestHLA.getTestingMethod());
            newHLA.setLaboratoryName(latestHLA.getLaboratoryName());
            newHLA.setCertificationNumber(latestHLA.getCertificationNumber());
            newHLA.setHlaString(latestHLA.getHlaString());
            newHLA.setIsHighResolution(latestHLA.getIsHighResolution());
            newHLA.setEventTimestamp(LocalDateTime.now());
            recipientHLAProfileRepository.save(newHLA);
        }
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
}
