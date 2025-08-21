package com.recipientservice.service;

import com.recipientservice.dto.*;
import com.recipientservice.enums.RequestStatus;
import com.recipientservice.enums.RequestType;
import com.recipientservice.exceptions.InvalidLocationException;
import com.recipientservice.exceptions.RecipientNotFoundException;
import com.recipientservice.kafka.EventPublisher;
import com.recipientservice.kafka.events.HLAProfileEvent;
import com.recipientservice.kafka.events.LocationEvent;
import com.recipientservice.kafka.events.ReceiveRequestEvent;
import com.recipientservice.kafka.events.RecipientEvent;
import com.recipientservice.model.*;
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
    private final EventPublisher eventPublisher;

    public RecipientServiceImpl(
            RecipientRepository recipientRepository,
            LocationRepository locationRepository,
            ReceiveRequestRepository receiveRequestRepository,
            EventPublisher eventPublisher
    ) {
        this.recipientRepository = recipientRepository;
        this.locationRepository = locationRepository;
        this.receiveRequestRepository = receiveRequestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RecipientDTO createRecipient(UUID userId, RegisterRecipientDTO dto) {
        Recipient recipient = recipientRepository.findByUserId(userId);

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
