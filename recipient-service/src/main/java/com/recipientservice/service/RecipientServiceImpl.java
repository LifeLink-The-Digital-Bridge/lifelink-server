package com.recipientservice.service;

import com.recipientservice.dto.*;
import com.recipientservice.exceptions.InvalidLocationException;
import com.recipientservice.exceptions.RecipientNotFoundException;
import com.recipientservice.model.*;
import com.recipientservice.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final LocationRepository locationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;

    public RecipientServiceImpl(RecipientRepository recipientRepository, LocationRepository locationRepository, ReceiveRequestRepository receiveRequestRepository) {
        this.recipientRepository = recipientRepository;
        this.locationRepository = locationRepository;
        this.receiveRequestRepository = receiveRequestRepository;
    }


    @Override
    public RecipientDTO createRecipient(UUID userId, RegisterRecipientDTO recipientDTO) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        if (recipient == null) {
            recipient = new Recipient();
            recipient.setUserId(userId);
        }
        recipient.setAvailability(recipientDTO.getAvailability());

        if (recipientDTO.getLocation() != null) {
            LocationDTO locDTO = recipientDTO.getLocation();
            if (locDTO.getAddressLine() == null || locDTO.getCity() == null || locDTO.getState() == null) {
                throw new InvalidLocationException("Location fields cannot be null");
            }
            Location location = mapLocationDTOToEntity(locDTO);
            recipient.setLocation(location);
        }


        if (recipientDTO.getMedicalDetails() != null) {
            MedicalDetails md = recipient.getMedicalDetails();
            if (md == null) md = new MedicalDetails();
            BeanUtils.copyProperties(recipientDTO.getMedicalDetails(), md);
            md.setRecipient(recipient);
            recipient.setMedicalDetails(md);
        }

        if (recipientDTO.getEligibilityCriteria() != null) {
            EligibilityCriteria ec = recipient.getEligibilityCriteria();
            if (ec == null) ec = new EligibilityCriteria();
            BeanUtils.copyProperties(recipientDTO.getEligibilityCriteria(), ec);
            ec.setRecipient(recipient);
            recipient.setEligibilityCriteria(ec);
        }

        if (recipientDTO.getConsentForm() != null) {
            ConsentForm cf = recipient.getConsentForm();
            if (cf == null) cf = new ConsentForm();
            BeanUtils.copyProperties(recipientDTO.getConsentForm(), cf);
            cf.setRecipient(recipient);
            recipient.setConsentForm(cf);
        }

        Recipient saved = recipientRepository.save(recipient);
        return mapRecipientToDTO(saved);
    }

    @Override
    public ReceiveRequestDTO createReceiveRequest(UUID userId, ReceiveRequestDTO requestDTO) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        if (recipient == null) {
            throw new RecipientNotFoundException("Recipient not found for userId: " + userId);
        }
        ReceiveRequest request = new ReceiveRequest();
        request.setRecipient(recipient);
        request.setRequestedBloodType(requestDTO.getRequestedBloodType());
        request.setRequestedOrgan(requestDTO.getRequestedOrgan());
        request.setUrgencyLevel(requestDTO.getUrgencyLevel());
        request.setQuantity(requestDTO.getQuantity());
        request.setRequestDate(requestDTO.getRequestDate());
        request.setStatus(requestDTO.getStatus());
        request.setNotes(requestDTO.getNotes());
        ReceiveRequest saved = receiveRequestRepository.save(request);
        return mapReceiveRequestToDTO(saved);
    }

    @Override
    public RecipientDTO getRecipientByUserId(UUID userId) {
        Recipient recipient = recipientRepository.findByUserId(userId);
        return recipient != null ? mapRecipientToDTO(recipient) : null;
    }

    @Override
    public RecipientDTO getRecipientById(UUID id) {
        Recipient recipient = recipientRepository.findById(id).orElse(null);
        return recipient != null ? mapRecipientToDTO(recipient) : null;
    }

    @Override
    public List<ReceiveRequestDTO> getReceiveRequestsByRecipientId(UUID recipientId) {
        List<ReceiveRequest> requests = receiveRequestRepository.findAllByRecipientId(recipientId);
        return requests.stream().map(this::mapReceiveRequestToDTO).collect(Collectors.toList());
    }

    private RecipientDTO mapRecipientToDTO(Recipient recipient) {
        if (recipient == null) return null;
        RecipientDTO dto = new RecipientDTO();
        dto.setId(recipient.getId());
        dto.setUserId(recipient.getUserId());
        dto.setAvailability(recipient.getAvailability());
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

    private Location mapLocationDTOToEntity(LocationDTO dto) {
        if (dto == null) return null;
        Location location = new Location();
        BeanUtils.copyProperties(dto, location);
        return locationRepository.save(location);
    }

    private ReceiveRequestDTO mapReceiveRequestToDTO(ReceiveRequest request) {
        if (request == null) return null;
        ReceiveRequestDTO dto = new ReceiveRequestDTO();
        dto.setId(request.getId());
        dto.setRecipientId(request.getRecipient().getId());
        dto.setRequestedBloodType(request.getRequestedBloodType());
        dto.setRequestedOrgan(request.getRequestedOrgan());
        dto.setUrgencyLevel(request.getUrgencyLevel());
        dto.setQuantity(request.getQuantity());
        dto.setRequestDate(request.getRequestDate());
        dto.setStatus(request.getStatus());
        dto.setNotes(request.getNotes());
        return dto;
    }
}
