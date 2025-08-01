package com.recipientservice.service;

import com.recipientservice.dto.*;
import com.recipientservice.exceptions.InvalidLocationException;
import com.recipientservice.exceptions.RecipientNotFoundException;
import com.recipientservice.kafka.EventPublisher;
import com.recipientservice.kafka.events.LocationEvent;
import com.recipientservice.kafka.events.ReceiveRequestEvent;
import com.recipientservice.kafka.events.RecipientEvent;
import com.recipientservice.model.*;
import com.recipientservice.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final LocationRepository locationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final EventPublisher eventPublisher;

    public RecipientServiceImpl(
            RecipientRepository recipientRepository,
            LocationRepository locationRepository,
            ReceiveRequestRepository receiveRequestRepository, EventPublisher eventPublisher
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
        }

        recipient.setAvailability(dto.getAvailability());

        if (dto.getLocation() != null) {
            validateLocationFields(dto.getLocation());
            recipient.setLocation(saveLocation(dto.getLocation()));
        }

        if (dto.getMedicalDetails() != null) {
            recipient.setMedicalDetails(copyMedicalDetails(dto.getMedicalDetails(), recipient.getMedicalDetails(), recipient));
        }

        if (dto.getEligibilityCriteria() != null) {
            recipient.setEligibilityCriteria(copyEligibility(dto.getEligibilityCriteria(), recipient.getEligibilityCriteria(), recipient));
        }

        if (dto.getConsentForm() != null) {
            recipient.setConsentForm(copyConsentForm(dto.getConsentForm(), recipient.getConsentForm(), recipient));
        }

        return mapRecipientToDTO(recipientRepository.save(recipient));
    }

    @Override
    public ReceiveRequestDTO createReceiveRequest(UUID userId, ReceiveRequestDTO requestDTO) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        if (recipient == null) {
            throw new RecipientNotFoundException("Recipient not found for userId: " + userId);
        }

        ReceiveRequest request = new ReceiveRequest();
        BeanUtils.copyProperties(requestDTO, request);
        request.setRecipient(recipient);
        ReceiveRequest saved = receiveRequestRepository.save(request);

        eventPublisher.publishReceiveRequestEvent(toReceiveRequestEvent(saved));
        eventPublisher.publishRecipientEvent(toRecipientEvent(recipient));

        if (recipient.getLocation() != null) {
            eventPublisher.publishRecipientLocationEvent(toLocationEvent(recipient));
        }

        return mapReceiveRequestToDTO(saved);
    }

    private ReceiveRequestEvent toReceiveRequestEvent(ReceiveRequest request) {
        ReceiveRequestEvent event = new ReceiveRequestEvent();
        event.setReceiveRequestId(request.getId());
        event.setRecipientId(request.getRecipient().getId());
        event.setRequestedBloodType(request.getRequestedBloodType());
        event.setRequestedOrgan(request.getRequestedOrgan());
        event.setUrgencyLevel(request.getUrgencyLevel());
        event.setQuantity(request.getQuantity());
        event.setRequestDate(request.getRequestDate());
        event.setStatus(request.getStatus());
        event.setNotes(request.getNotes());
        return event;
    }

    private RecipientEvent toRecipientEvent(Recipient recipient) {
        RecipientEvent event = new RecipientEvent();
        event.setRecipientId(recipient.getId());
        event.setUserId(recipient.getUserId());
        event.setAvailability(recipient.getAvailability());

        if (recipient.getMedicalDetails() != null) {
            event.setMedicalDetailsId(recipient.getMedicalDetails().getId());
            event.setDiagnosis(recipient.getMedicalDetails().getDiagnosis());
            event.setAllergies(recipient.getMedicalDetails().getAllergies());
            event.setCurrentMedications(recipient.getMedicalDetails().getCurrentMedications());
            event.setAdditionalNotes(recipient.getMedicalDetails().getAdditionalNotes());
        }

        if (recipient.getEligibilityCriteria() != null) {
            event.setEligibilityCriteriaId(recipient.getEligibilityCriteria().getId());
            event.setMedicallyEligible(recipient.getEligibilityCriteria().getMedicallyEligible());
            event.setLegalClearance(recipient.getEligibilityCriteria().getLegalClearance());
            event.setEligibilityNotes(recipient.getEligibilityCriteria().getNotes());
            event.setLastReviewed(recipient.getEligibilityCriteria().getLastReviewed());
        }

        return event;
    }

    private LocationEvent toLocationEvent(Recipient recipient) {
        Location location = recipient.getLocation();
        if (location == null) return null;

        LocationEvent event = new LocationEvent();
        event.setLocationId(location.getId());
        event.setRecipientId(recipient.getId());
        event.setAddressLine(location.getAddressLine());
        event.setLandmark(location.getLandmark());
        event.setArea(location.getArea());
        event.setCity(location.getCity());
        event.setDistrict(location.getDistrict());
        event.setState(location.getState());
        event.setCountry(location.getCountry());
        event.setPincode(location.getPincode());
        event.setLatitude(location.getLatitude());
        event.setLongitude(location.getLongitude());

        return event;
    }


    @Override
    public RecipientDTO getRecipientByUserId(UUID userId) {
        return mapRecipientToDTO(recipientRepository.findByUserId(userId));
    }

    @Override
    public RecipientDTO getRecipientById(UUID id) {
        return mapRecipientToDTO(recipientRepository.findById(id).orElse(null));
    }

    @Override
    public List<ReceiveRequestDTO> getReceiveRequestsByRecipientId(UUID recipientId) {
        return receiveRequestRepository.findAllByRecipientId(recipientId)
                .stream()
                .map(this::mapReceiveRequestToDTO)
                .collect(Collectors.toList());
    }

    private void validateLocationFields(LocationDTO locationDTO) {
        if (locationDTO.getAddressLine() == null || locationDTO.getCity() == null || locationDTO.getState() == null) {
            throw new InvalidLocationException("Location fields cannot be null");
        }
    }

    private Location saveLocation(LocationDTO dto) {
        Location location = new Location();
        BeanUtils.copyProperties(dto, location);
        return locationRepository.save(location);
    }

    private MedicalDetails copyMedicalDetails(MedicalDetailsDTO dto, MedicalDetails existing, Recipient recipient) {
        if (existing == null) existing = new MedicalDetails();
        BeanUtils.copyProperties(dto, existing);
        existing.setRecipient(recipient);
        return existing;
    }

    private EligibilityCriteria copyEligibility(EligibilityCriteriaDTO dto, EligibilityCriteria existing, Recipient recipient) {
        if (existing == null) existing = new EligibilityCriteria();
        BeanUtils.copyProperties(dto, existing);
        existing.setRecipient(recipient);
        return existing;
    }

    private ConsentForm copyConsentForm(ConsentFormDTO dto, ConsentForm existing, Recipient recipient) {
        if (existing == null) existing = new ConsentForm();
        BeanUtils.copyProperties(dto, existing);
        existing.setRecipient(recipient);
        return existing;
    }

    private RecipientDTO mapRecipientToDTO(Recipient recipient) {
        if (recipient == null) return null;

        RecipientDTO dto = new RecipientDTO();
        BeanUtils.copyProperties(recipient, dto);
        dto.setLocation(mapLocationToDTO(recipient.getLocation()));

        if (recipient.getMedicalDetails() != null) {
            MedicalDetailsDTO mdDTO = new MedicalDetailsDTO();
            BeanUtils.copyProperties(recipient.getMedicalDetails(), mdDTO);
            dto.setMedicalDetails(mdDTO);
        }

        if (recipient.getEligibilityCriteria() != null) {
            EligibilityCriteriaDTO ecDTO = new EligibilityCriteriaDTO();
            BeanUtils.copyProperties(recipient.getEligibilityCriteria(), ecDTO);
            dto.setEligibilityCriteria(ecDTO);
        }

        if (recipient.getConsentForm() != null) {
            ConsentFormDTO cfDTO = new ConsentFormDTO();
            BeanUtils.copyProperties(recipient.getConsentForm(), cfDTO);
            dto.setConsentForm(cfDTO);
        }

        return dto;
    }

    private LocationDTO mapLocationToDTO(Location location) {
        if (location == null) return null;
        LocationDTO dto = new LocationDTO();
        BeanUtils.copyProperties(location, dto);
        return dto;
    }

    private ReceiveRequestDTO mapReceiveRequestToDTO(ReceiveRequest request) {
        if (request == null) return null;

        ReceiveRequestDTO dto = new ReceiveRequestDTO();
        BeanUtils.copyProperties(request, dto);
        dto.setRecipientId(request.getRecipient().getId());
        return dto;
    }
}
