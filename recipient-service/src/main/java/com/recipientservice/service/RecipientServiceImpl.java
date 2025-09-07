package com.recipientservice.service;

import com.recipientservice.dto.*;
import com.recipientservice.enums.*;
import com.recipientservice.exceptions.InvalidLocationException;
import com.recipientservice.exceptions.RecipientNotFoundException;
import com.recipientservice.kafka.EventPublisher;
import com.recipientservice.kafka.events.HLAProfileEvent;
import com.recipientservice.kafka.events.LocationEvent;
import com.recipientservice.kafka.events.ReceiveRequestEvent;
import com.recipientservice.kafka.events.RecipientEvent;
import com.recipientservice.model.*;
import com.recipientservice.model.history.*;
import com.recipientservice.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final LocationRepository locationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final RecipientHistoryRepository recipientHistoryRepository;
    private final EventPublisher eventPublisher;
    private final ProfileLockService profileLockService;

    public RecipientServiceImpl(
            RecipientRepository recipientRepository,
            LocationRepository locationRepository,
            ReceiveRequestRepository receiveRequestRepository,
            RecipientHistoryRepository recipientHistoryRepository,
            EventPublisher eventPublisher,
            ProfileLockService profileLockService
    ) {
        this.recipientRepository = recipientRepository;
        this.locationRepository = locationRepository;
        this.receiveRequestRepository = receiveRequestRepository;
        this.recipientHistoryRepository = recipientHistoryRepository;
        this.eventPublisher = eventPublisher;
        this.profileLockService = profileLockService;
    }

    @Override
    public RecipientDTO saveOrUpdateRecipient(UUID userId, RegisterRecipientDTO dto) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        
        if (recipient != null && profileLockService.isRecipientProfileLocked(recipient.getId())) {
            throw new IllegalStateException(profileLockService.getProfileLockReason(recipient.getId()));
        }

        if (recipient == null) {
            recipient = new Recipient();
            recipient.setUserId(userId);
            recipient.setAddresses(new ArrayList<>());
        }

        recipient.setAvailability(dto.getAvailability());

        MedicalDetails medicalDetails = recipient.getMedicalDetails();
        if (medicalDetails == null) {
            medicalDetails = new MedicalDetails();
            medicalDetails.setRecipient(recipient);
        } else {
            dto.getMedicalDetails().setId(medicalDetails.getId());
        }
        BeanUtils.copyProperties(dto.getMedicalDetails(), medicalDetails);
        recipient.setMedicalDetails(medicalDetails);

        EligibilityCriteria eligibilityCriteria = recipient.getEligibilityCriteria();
        if (eligibilityCriteria == null) {
            eligibilityCriteria = new EligibilityCriteria();
            eligibilityCriteria.setRecipient(recipient);
        } else {
            dto.getEligibilityCriteria().setId(eligibilityCriteria.getId());
        }
        BeanUtils.copyProperties(dto.getEligibilityCriteria(), eligibilityCriteria);
        recipient.setEligibilityCriteria(eligibilityCriteria);

        ConsentForm consentForm = recipient.getConsentForm();
        if (consentForm == null) {
            consentForm = new ConsentForm();
            consentForm.setRecipient(recipient);
        } else {
            dto.getConsentForm().setId(consentForm.getId());
        }
        BeanUtils.copyProperties(dto.getConsentForm(), consentForm);
        consentForm.setUserId(userId);
        recipient.setConsentForm(consentForm);

        if (dto.getHlaProfile() != null) {
            validateHLAProfileDTO(dto.getHlaProfile());

            HLAProfile hlaProfile = recipient.getHlaProfile();
            if (hlaProfile == null) {
                hlaProfile = new HLAProfile();
                hlaProfile.setRecipient(recipient);
            } else {
                dto.getHlaProfile().setId(hlaProfile.getId());
            }
            BeanUtils.copyProperties(dto.getHlaProfile(), hlaProfile);
            recipient.setHlaProfile(hlaProfile);
        }

        List<Location> freshAddresses = new ArrayList<>();
        if (dto.getAddresses() != null && !dto.getAddresses().isEmpty()) {
            for (LocationDTO locDTO : dto.getAddresses()) {
                validateLocationDTO(locDTO);

                if (locDTO.getId() != null) {
                    Location existing = locationRepository.findById(locDTO.getId())
                            .orElseThrow(() -> new InvalidLocationException("Address not found"));
                    BeanUtils.copyProperties(locDTO, existing, "id", "recipient");
                    existing.setRecipient(recipient);
                    freshAddresses.add(existing);
                } else {
                    Location location = new Location();
                    BeanUtils.copyProperties(locDTO, location);
                    location.setRecipient(recipient);
                    freshAddresses.add(location);
                }
            }
        }
        recipient.setAddresses(freshAddresses);

        Recipient savedRecipient = recipientRepository.save(recipient);
        return getRecipientDTO(savedRecipient);
    }

    @Override
    public ReceiveRequestDTO createReceiveRequest(UUID userId, CreateReceiveRequestDTO requestDTO) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (requestDTO == null) {
            throw new IllegalArgumentException("CreateReceiveRequestDTO cannot be null");
        }

        Recipient recipient = recipientRepository.findByUserId(userId);
        if (recipient == null) {
            throw new RecipientNotFoundException("Recipient not found for userId: " + userId);
        }

        validateRecipientProfileComplete(recipient);

        Location location = null;
        if (requestDTO.getLocationId() != null) {
            location = locationRepository.findById(requestDTO.getLocationId())
                    .orElseThrow(() -> new InvalidLocationException("Invalid location ID"));
        }

        System.out.println("CreateReceiveRequestDTO: " + requestDTO);
        System.out.println("Location: " + location);

        ReceiveRequest request = new ReceiveRequest();
        copyCreateReceiveRequestProperties(requestDTO, request);
        request.setRecipient(recipient);
        request.setLocation(location);
        request.setStatus(RequestStatus.PENDING);

        ReceiveRequest saved = receiveRequestRepository.save(request);
        ReceiveRequestDTO responseDTO = convertToDTO(saved);

        eventPublisher.publishReceiveRequestEvent(getReceiveRequestEvent(responseDTO));
        eventPublisher.publishRecipientEvent(getRecipientEvent(recipient));
        if (location != null) {
            eventPublisher.publishRecipientLocationEvent(getLocationEvent(location, recipient.getId()));
        }
        if (recipient.getHlaProfile() != null && requestDTO.getRequestType() == RequestType.ORGAN) {
            eventPublisher.publishHLAProfileEvent(getHLAProfileEvent(recipient.getHlaProfile(), recipient.getId()));
        }
        System.out.println("Event published successfully.");
        return responseDTO;
    }

    private HLAProfileEvent getHLAProfileEvent(HLAProfile hlaProfile, UUID recipientId) {
        if (hlaProfile == null) return null;

        HLAProfileEvent event = new HLAProfileEvent();
        BeanUtils.copyProperties(hlaProfile, event);
        event.setRecipientId(recipientId);
        return event;
    }

    private void copyCreateReceiveRequestProperties(CreateReceiveRequestDTO source, ReceiveRequest target) {
        target.setRequestType(source.getRequestType());
        target.setRequestedBloodType(source.getRequestedBloodType());
        target.setRequestedOrgan(source.getRequestedOrgan());
        target.setRequestedTissue(source.getRequestedTissue());
        target.setRequestedStemCellType(source.getRequestedStemCellType());
        target.setUrgencyLevel(source.getUrgencyLevel());
        target.setQuantity(source.getQuantity());
        target.setRequestDate(source.getRequestDate());
        target.setNotes(source.getNotes());
    }

    @Override
    public RecipientDTO getRecipientById(UUID id) {
        Recipient savedRecipient = recipientRepository.findById(id)
                .orElseThrow(() -> new RecipientNotFoundException("Recipient with id " + id + " not found!"));
        return getRecipientDTO(savedRecipient);
    }

    @Override
    public RecipientDTO getRecipientByUserId(UUID userId) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        if (recipient == null) throw new RecipientNotFoundException("Recipient not found");
        return getRecipientDTO(recipient);
    }

    @Override
    public List<ReceiveRequestDTO> getReceiveRequestsByRecipientId(UUID recipientId) {
        List<ReceiveRequest> requests = receiveRequestRepository.findAllByRecipientId(recipientId);
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiveRequestDTO> getReceiveRequestsByUserId(UUID userId) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        if (recipient == null) {
            throw new RecipientNotFoundException("Recipient not found");
        }
        return getReceiveRequestsByRecipientId(recipient.getId());
    }

    @Override
    public void updateRequestStatus(UUID requestId, RequestStatus status) {
        receiveRequestRepository.findById(requestId)
                .ifPresent(request -> {
                    request.setStatus(status);
                    receiveRequestRepository.save(request);
                });
    }

    @Override
    public String getRequestStatus(UUID requestId) {
        return receiveRequestRepository.findById(requestId)
                .map(request -> request.getStatus().toString())
                .orElseThrow(() -> new RecipientNotFoundException("Request not found"));
    }

    @Override
    public ReceiveRequestDTO getRequestById(UUID requestId) {
        ReceiveRequest request = receiveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RecipientNotFoundException("Request not found"));
        return convertToDTO(request);
    }

    @Override
    public void createRecipientHistory(CreateRecipientHistoryRequest request) {
        RecipientSnapshotHistory recipientSnapshot = new RecipientSnapshotHistory();
        recipientSnapshot.setOriginalRecipientId(request.getRecipientId());
        recipientSnapshot.setUserId(request.getRecipientUserId());
        recipientSnapshot.setAvailability(Availability.valueOf(request.getAvailability()));

        RecipientMedicalDetailsSnapshotHistory medicalSnapshot = new RecipientMedicalDetailsSnapshotHistory();
        medicalSnapshot.setDiagnosis(request.getDiagnosis());
        medicalSnapshot.setAllergies(request.getAllergies());
        medicalSnapshot.setCurrentMedications(request.getCurrentMedications());
        medicalSnapshot.setAdditionalNotes(request.getAdditionalNotes());

        RecipientEligibilityCriteriaSnapshotHistory eligibilitySnapshot = new RecipientEligibilityCriteriaSnapshotHistory();
        eligibilitySnapshot.setMedicallyEligible(request.getMedicallyEligible());
        eligibilitySnapshot.setLegalClearance(request.getLegalClearance());
        eligibilitySnapshot.setNotes(request.getNotes());
        eligibilitySnapshot.setLastReviewed(request.getLastReviewed());

        RecipientHLAProfileSnapshotHistory hlaSnapshot = new RecipientHLAProfileSnapshotHistory();
        hlaSnapshot.setHlaA1(request.getHlaA1());
        hlaSnapshot.setHlaA2(request.getHlaA2());
        hlaSnapshot.setHlaB1(request.getHlaB1());
        hlaSnapshot.setHlaB2(request.getHlaB2());
        hlaSnapshot.setHlaC1(request.getHlaC1());
        hlaSnapshot.setHlaC2(request.getHlaC2());
        hlaSnapshot.setHlaDr1(request.getHlaDR1());
        hlaSnapshot.setHlaDr2(request.getHlaDR2());
        hlaSnapshot.setHlaDq1(request.getHlaDQ1());
        hlaSnapshot.setHlaDq2(request.getHlaDQ2());
        hlaSnapshot.setHlaDP1(request.getHlaDP1());
        hlaSnapshot.setHlaDP2(request.getHlaDP2());
        hlaSnapshot.setTestingDate(request.getTestingDate());
        hlaSnapshot.setTestMethod(request.getTestingMethod());
        hlaSnapshot.setLaboratoryName(request.getLaboratoryName());
        hlaSnapshot.setCertificationNumber(request.getCertificationNumber());
        hlaSnapshot.setHlaString(request.getHlaString());
        hlaSnapshot.setIsHighResolution(request.getIsHighResolution());
        hlaSnapshot.setResolutionLevel(request.getCertificationNumber());

        RecipientConsentFormSnapshotHistory consentSnapshot = new RecipientConsentFormSnapshotHistory();
        consentSnapshot.setUserId(request.getRecipientUserId());
        consentSnapshot.setIsConsented(request.getIsConsented());
        consentSnapshot.setConsentedAt(request.getConsentedAt());

        ReceiveRequestSnapshotHistory requestSnapshot = new ReceiveRequestSnapshotHistory();
        requestSnapshot.setOriginalRequestId(request.getReceiveRequestId());
        requestSnapshot.setRequestType(RequestType.valueOf(request.getRequestType()));
        if (request.getRequestedBloodType() != null) {
            requestSnapshot.setRequestedBloodType(BloodType.valueOf(request.getRequestedBloodType()));
        }
        if (request.getRequestedOrgan() != null) {
            requestSnapshot.setRequestedOrgan(OrganType.valueOf(request.getRequestedOrgan()));
        }
        if (request.getRequestedTissue() != null) {
            requestSnapshot.setRequestedTissue(TissueType.valueOf(request.getRequestedTissue()));
        }
        if (request.getRequestedStemCellType() != null) {
            requestSnapshot.setRequestedStemCellType(StemCellType.valueOf(request.getRequestedStemCellType()));
        }
        requestSnapshot.setUrgencyLevel(UrgencyLevel.valueOf(request.getUrgencyLevel()));
        requestSnapshot.setQuantity(request.getQuantity());
        requestSnapshot.setRequestDate(request.getRequestDate());
        requestSnapshot.setStatus(RequestStatus.valueOf(request.getRequestStatus()));
        requestSnapshot.setNotes(request.getRequestNotes());

        RecipientHistory history = new RecipientHistory();
        history.setRecipientSnapshot(recipientSnapshot);
        history.setMedicalDetailsSnapshot(medicalSnapshot);
        history.setEligibilityCriteriaSnapshot(eligibilitySnapshot);
        history.setHlaProfileSnapshot(hlaSnapshot);
        history.setConsentFormSnapshot(consentSnapshot);
        history.setReceiveRequestSnapshot(requestSnapshot);
        history.setMatchId(request.getMatchId());
        history.setDonationId(request.getDonationId());
        history.setDonorUserId(request.getDonorUserId());
        history.setMatchedAt(request.getMatchedAt());
        history.setCompletedAt(request.getCompletedAt());

        recipientHistoryRepository.save(history);
    }
    
    @Override
    public List<RecipientHistoryDTO> getRecipientHistory(UUID userId) {
        List<RecipientHistory> histories = recipientHistoryRepository.findByRecipientSnapshot_UserId(userId);
        return histories.stream()
                .map(this::convertToHistoryDTO)
                .collect(Collectors.toList());
    }
    
    private RecipientHistoryDTO convertToHistoryDTO(RecipientHistory history) {
        RecipientHistoryDTO dto = new RecipientHistoryDTO();
        dto.setMatchId(history.getMatchId());
        dto.setDonationId(history.getDonationId());
        dto.setDonorUserId(history.getDonorUserId());
        dto.setMatchedAt(history.getMatchedAt());
        dto.setCompletedAt(history.getCompletedAt());
        
        if (history.getReceiveRequestSnapshot() != null) {
            ReceiveRequestDTO requestDTO = new ReceiveRequestDTO();
            requestDTO.setId(history.getReceiveRequestSnapshot().getOriginalRequestId());
            requestDTO.setRequestDate(history.getReceiveRequestSnapshot().getRequestDate());
            requestDTO.setStatus(history.getReceiveRequestSnapshot().getStatus());
            requestDTO.setRequestType(history.getReceiveRequestSnapshot().getRequestType());
            dto.setReceiveRequestSnapshot(requestDTO);
        }
        
        return dto;
    }

    private RecipientDTO getRecipientDTO(Recipient savedRecipient) {
        RecipientDTO responseDTO = new RecipientDTO();
        responseDTO.setId(savedRecipient.getId());
        responseDTO.setUserId(savedRecipient.getUserId());
        responseDTO.setAvailability(savedRecipient.getAvailability());

        if (savedRecipient.getMedicalDetails() != null) {
            MedicalDetailsDTO mdDTO = new MedicalDetailsDTO();
            BeanUtils.copyProperties(savedRecipient.getMedicalDetails(), mdDTO);
            responseDTO.setMedicalDetails(mdDTO);
        }

        if (savedRecipient.getEligibilityCriteria() != null) {
            EligibilityCriteriaDTO ecDTO = new EligibilityCriteriaDTO();
            BeanUtils.copyProperties(savedRecipient.getEligibilityCriteria(), ecDTO);
            responseDTO.setEligibilityCriteria(ecDTO);
        }

        if (savedRecipient.getConsentForm() != null) {
            ConsentFormDTO cfDTO = new ConsentFormDTO();
            BeanUtils.copyProperties(savedRecipient.getConsentForm(), cfDTO);
            responseDTO.setConsentForm(cfDTO);
        }

        if (savedRecipient.getHlaProfile() != null) {
            HLAProfileDTO hlaDTO = new HLAProfileDTO();
            BeanUtils.copyProperties(savedRecipient.getHlaProfile(), hlaDTO);
            responseDTO.setHlaProfile(hlaDTO);
        }

        if (savedRecipient.getAddresses() != null && !savedRecipient.getAddresses().isEmpty()) {
            List<LocationDTO> locDTOList = savedRecipient.getAddresses().stream().map(location -> {
                LocationDTO locDTO = new LocationDTO();
                BeanUtils.copyProperties(location, locDTO);
                return locDTO;
            }).collect(Collectors.toList());
            responseDTO.setAddresses(locDTOList);
        } else {
            responseDTO.setAddresses(new ArrayList<>());
        }

        return responseDTO;
    }

    private void validateHLAProfileDTO(HLAProfileDTO hlaProfileDTO) {
        if (hlaProfileDTO.getTestingDate() == null) {
            throw new IllegalArgumentException("Testing date is required for HLA profile");
        }

        if (hlaProfileDTO.getTestingDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Testing date cannot be in the future");
        }

        if (hlaProfileDTO.getHlaA1() == null && hlaProfileDTO.getHlaA2() == null &&
                hlaProfileDTO.getHlaB1() == null && hlaProfileDTO.getHlaB2() == null) {
            throw new IllegalArgumentException("At least some HLA markers must be provided");
        }

        if (hlaProfileDTO.getLaboratoryName() == null || hlaProfileDTO.getLaboratoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Laboratory name is required for HLA profile");
        }
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

    private LocationEvent getLocationEvent(Location location, UUID recipientId) {
        if (location == null) return null;
        LocationEvent event = new LocationEvent();
        BeanUtils.copyProperties(location, event);
        event.setLocationId(location.getId());
        if (recipientId != null) event.setRecipientId(recipientId);
        return event;
    }

    private RecipientEvent getRecipientEvent(Recipient recipient) {
        if (recipient == null) return null;
        RecipientEvent recipientEvent = new RecipientEvent();
        BeanUtils.copyProperties(recipient, recipientEvent);
        recipientEvent.setRecipientId(recipient.getId());
        if (recipient.getAvailability() != null) {
            recipientEvent.setAvailability(recipient.getAvailability());
        }

        if (recipient.getMedicalDetails() != null) {
            recipientEvent.setMedicalDetailsId(recipient.getMedicalDetails().getId());
            recipientEvent.setDiagnosis(recipient.getMedicalDetails().getDiagnosis());
            recipientEvent.setAllergies(recipient.getMedicalDetails().getAllergies());
            recipientEvent.setCurrentMedications(recipient.getMedicalDetails().getCurrentMedications());
            recipientEvent.setAdditionalNotes(recipient.getMedicalDetails().getAdditionalNotes());
        }

        if (recipient.getEligibilityCriteria() != null) {
            recipientEvent.setEligibilityCriteriaId(recipient.getEligibilityCriteria().getId());
            recipientEvent.setMedicallyEligible(recipient.getEligibilityCriteria().getMedicallyEligible());
            recipientEvent.setLegalClearance(recipient.getEligibilityCriteria().getLegalClearance());
            recipientEvent.setEligibilityNotes(recipient.getEligibilityCriteria().getNotes());
            recipientEvent.setLastReviewed(recipient.getEligibilityCriteria().getLastReviewed());
        }

        return recipientEvent;
    }

    public static ReceiveRequestEvent getReceiveRequestEvent(ReceiveRequestDTO requestDTO) {
        if (requestDTO == null) return null;
        ReceiveRequestEvent event = new ReceiveRequestEvent();
        event.setReceiveRequestId(requestDTO.getId());
        event.setRecipientId(requestDTO.getRecipientId());
        event.setLocationId(requestDTO.getLocationId());
        event.setRequestType(requestDTO.getRequestType());
        event.setRequestedBloodType(requestDTO.getRequestedBloodType());
        event.setRequestedOrgan(requestDTO.getRequestedOrgan());
        event.setRequestedTissue(requestDTO.getRequestedTissue());
        event.setRequestedStemCellType(requestDTO.getRequestedStemCellType());
        event.setUrgencyLevel(requestDTO.getUrgencyLevel());
        event.setQuantity(requestDTO.getQuantity());
        event.setRequestDate(requestDTO.getRequestDate());
        event.setStatus(requestDTO.getStatus());
        event.setNotes(requestDTO.getNotes());
        return event;
    }


    private ReceiveRequestDTO convertToDTO(ReceiveRequest request) {
        if (request == null) return null;

        ReceiveRequestDTO dto = new ReceiveRequestDTO();
        dto.setId(request.getId());
        dto.setRecipientId(request.getRecipient().getId());
        dto.setLocationId(request.getLocation() != null ? request.getLocation().getId() : null);
        dto.setRequestType(request.getRequestType());
        dto.setRequestedBloodType(request.getRequestedBloodType());
        dto.setRequestedOrgan(request.getRequestedOrgan());
        dto.setRequestedTissue(request.getRequestedTissue());
        dto.setRequestedStemCellType(request.getRequestedStemCellType());
        dto.setUrgencyLevel(request.getUrgencyLevel());
        dto.setQuantity(request.getQuantity());
        dto.setRequestDate(request.getRequestDate());
        dto.setStatus(request.getStatus());
        dto.setNotes(request.getNotes());

        return dto;
    }


    private void validateRecipientProfileComplete(Recipient recipient) {
        if (recipient.getMedicalDetails() == null ||
                recipient.getEligibilityCriteria() == null ||
                recipient.getConsentForm() == null ||
                !Boolean.TRUE.equals(recipient.getConsentForm().getIsConsented())) {
            throw new RecipientNotFoundException(
                    "Recipient profile is incomplete. Please complete all details including medical details, eligibility criteria, and consent form before creating requests."
            );
        }
    }
}
