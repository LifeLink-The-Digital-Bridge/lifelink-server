package com.donorservice.service;

import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.enums.*;
import com.donorservice.exception.InvalidDonorProfileException;
import com.donorservice.exception.InvalidLocationException;
import com.donorservice.exception.ResourceNotFoundException;
import com.donorservice.exception.UnsupportedDonationTypeException;
import com.donorservice.kafka.EventPublisher;
import com.donorservice.kafka.event.DonationEvent;
import com.donorservice.kafka.event.DonorEvent;
import com.donorservice.kafka.event.HLAProfileEvent;
import com.donorservice.kafka.event.LocationEvent;
import com.donorservice.model.*;
import com.donorservice.model.history.*;
import com.donorservice.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;
    private final DonorHistoryRepository donorHistoryRepository;
    private final LocationRepository locationRepository;
    private final EventPublisher eventPublisher;
    private final ProfileLockService profileLockService;

    public DonorServiceImpl(DonorRepository donorRepository,
                            DonationRepository donationRepository,
                            DonorHistoryRepository donorHistoryRepository,
                            UserClient userClient,
                            LocationRepository locationRepository,
                            EventPublisher eventPublisher,
                            ProfileLockService profileLockService) {
        this.donorRepository = donorRepository;
        this.donationRepository = donationRepository;
        this.donorHistoryRepository = donorHistoryRepository;
        this.locationRepository = locationRepository;
        this.eventPublisher = eventPublisher;
        this.profileLockService = profileLockService;
    }

    @Override
    public DonorDTO saveOrUpdateDonor(UUID userId, RegisterDonor donorDTO) {
        Donor existingDonor = donorRepository.findByUserId(userId);
        
        if (existingDonor != null && profileLockService.isDonorProfileLocked(existingDonor.getId())) {
            throw new IllegalStateException(profileLockService.getProfileLockReason(existingDonor.getId()));
        }
        Donor donor = donorRepository.findByUserId(userId);

        if (donor == null) {
            donor = new Donor();
            donor.setUserId(userId);
            donor.setAddresses(new ArrayList<>());
        }
        donor.setRegistrationDate(donorDTO.getRegistrationDate());
        donor.setStatus(donorDTO.getStatus());

        MedicalDetails medicalDetails = donor.getMedicalDetails();
        if (medicalDetails == null) {
            medicalDetails = new MedicalDetails();
            medicalDetails.setDonor(donor);
        } else {
            donorDTO.getMedicalDetails().setId(medicalDetails.getId());
        }
        BeanUtils.copyProperties(donorDTO.getMedicalDetails(), medicalDetails);
        donor.setMedicalDetails(medicalDetails);

        EligibilityCriteria eligibilityCriteria = donor.getEligibilityCriteria();
        if (eligibilityCriteria == null) {
            eligibilityCriteria = new EligibilityCriteria();
            eligibilityCriteria.setDonor(donor);
        } else {
            donorDTO.getEligibilityCriteria().setId(eligibilityCriteria.getId());
        }
        BeanUtils.copyProperties(donorDTO.getEligibilityCriteria(), eligibilityCriteria);
        donor.setEligibilityCriteria(eligibilityCriteria);

        ConsentForm consentForm = donor.getConsentForm();
        if (consentForm == null) {
            consentForm = new ConsentForm();
            consentForm.setDonor(donor);
        } else {
            donorDTO.getConsentForm().setId(consentForm.getId());
        }
        BeanUtils.copyProperties(donorDTO.getConsentForm(), consentForm);
        consentForm.setUserId(userId);
        donor.setConsentForm(consentForm);

        if (donorDTO.getHlaProfile() != null) {
            HLAProfile hlaProfile = donor.getHlaProfile();
            if (hlaProfile == null) {
                hlaProfile = new HLAProfile();
                hlaProfile.setDonor(donor);
            } else {
                donorDTO.getHlaProfile().setId(hlaProfile.getId());
            }
            BeanUtils.copyProperties(donorDTO.getHlaProfile(), hlaProfile, "id", "donor");
            hlaProfile.setDonor(donor);
            donor.setHlaProfile(hlaProfile);
        }

        List<Location> freshAddresses = new ArrayList<>();
        if (donorDTO.getAddresses() != null && !donorDTO.getAddresses().isEmpty()) {
            for (LocationDTO locDTO : donorDTO.getAddresses()) {
                validateLocationDTO(locDTO);

                if (locDTO.getId() != null) {
                    Location existing = locationRepository.findById(locDTO.getId())
                            .orElseThrow(() -> new InvalidLocationException("Address not found"));
                    BeanUtils.copyProperties(locDTO, existing, "id", "donor");
                    existing.setDonor(donor);
                    freshAddresses.add(existing);
                } else {
                    Location location = new Location();
                    BeanUtils.copyProperties(locDTO, location);
                    location.setDonor(donor);
                    freshAddresses.add(location);
                }
            }
        }
        donor.setAddresses(freshAddresses);

        Donor savedDonor = donorRepository.save(donor);
        return getDonorDTO(savedDonor);
    }

    @Override
    public DonorDTO getDonorById(UUID id) {
        Donor savedDonor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor with id " + id + " not found!"));
        return getDonorDTO(savedDonor);
    }

    @Override
    public List<DonationDTO> getDonationsByUserId(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) {
            throw new ResourceNotFoundException("Donor not found");
        }
        return getDonationsByDonorId(donor.getId());
    }

    @Override
    public DonorDTO getDonorByUserId(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) throw new ResourceNotFoundException("Donor not found");
        return getDonorDTO(donor);
    }

    @Override
    public void updateDonationStatus(UUID donationId, DonationStatus status) {
        donationRepository.findById(donationId)
                .ifPresent(donation -> {
                    donation.setStatus(status);
                    donationRepository.save(donation);
                });
    }

    @Override
    public String getDonationStatus(UUID donationId) {
        return donationRepository.findById(donationId)
                .map(donation -> donation.getStatus().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
    }

    @Override
    public DonationDTO getDonationById(UUID donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
        return convertToDTO(donation);
    }

    @Override
    public void createDonationHistory(CreateDonationHistoryRequest request) {
        DonorSnapshotHistory donorSnapshot = new DonorSnapshotHistory();
        donorSnapshot.setOriginalDonorId(request.getDonorId());
        donorSnapshot.setUserId(request.getDonorUserId());
        donorSnapshot.setRegistrationDate(request.getRegistrationDate());
        donorSnapshot.setStatus(DonorStatus.valueOf(request.getDonorStatus()));

        MedicalDetailsSnapshotHistory medicalSnapshot = new MedicalDetailsSnapshotHistory();
        medicalSnapshot.setHemoglobinLevel(request.getHemoglobinLevel());
        medicalSnapshot.setBloodPressure(request.getBloodPressure());
        medicalSnapshot.setHasDiseases(request.getHasDiseases());
        medicalSnapshot.setTakingMedication(request.getTakingMedication());
        medicalSnapshot.setDiseaseDescription(request.getDiseaseDescription());
        medicalSnapshot.setCurrentMedications(request.getCurrentMedications());
        medicalSnapshot.setLastMedicalCheckup(request.getLastMedicalCheckup());
        medicalSnapshot.setMedicalHistory(request.getMedicalHistory());
        medicalSnapshot.setHasInfectiousDiseases(request.getHasInfectiousDiseases());
        medicalSnapshot.setInfectiousDiseaseDetails(request.getInfectiousDiseaseDetails());
        medicalSnapshot.setCreatinineLevel(request.getCreatinineLevel());
        medicalSnapshot.setLiverFunctionTests(request.getLiverFunctionTests());
        medicalSnapshot.setCardiacStatus(request.getCardiacStatus());
        medicalSnapshot.setPulmonaryFunction(request.getPulmonaryFunction());
        medicalSnapshot.setOverallHealthStatus(request.getOverallHealthStatus());

        EligibilityCriteriaSnapshotHistory eligibilitySnapshot = new EligibilityCriteriaSnapshotHistory();
        eligibilitySnapshot.setAgeEligible(request.getAgeEligible());
        eligibilitySnapshot.setAge(request.getAge());
        eligibilitySnapshot.setDob(request.getDob());
        eligibilitySnapshot.setWeightEligible(request.getWeightEligible());
        eligibilitySnapshot.setWeight(request.getWeight());
        eligibilitySnapshot.setMedicalClearance(request.getMedicalClearance());
        eligibilitySnapshot.setRecentTattooOrPiercing(request.getRecentTattooOrPiercing());
        eligibilitySnapshot.setRecentTravelDetails(request.getRecentTravelDetails());
        eligibilitySnapshot.setRecentVaccination(request.getRecentVaccination());
        eligibilitySnapshot.setRecentSurgery(request.getRecentSurgery());
        eligibilitySnapshot.setChronicDiseases(request.getChronicDiseases());
        eligibilitySnapshot.setAllergies(request.getAllergies());
        eligibilitySnapshot.setLastDonationDate(request.getLastDonationDate());
        eligibilitySnapshot.setHeight(request.getHeight());
        eligibilitySnapshot.setBodyMassIndex(request.getBodyMassIndex());
        eligibilitySnapshot.setBodySize(request.getBodySize());
        eligibilitySnapshot.setIsLivingDonor(request.getIsLivingDonor());

        HLAProfileSnapshotHistory hlaSnapshot = new HLAProfileSnapshotHistory();
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

        ConsentFormSnapshotHistory consentSnapshot = new ConsentFormSnapshotHistory();
        consentSnapshot.setUserId(request.getDonorUserId());
        consentSnapshot.setIsConsented(request.getIsConsented());
        consentSnapshot.setConsentedAt(request.getConsentedAt());

        DonationSnapshotHistory donationSnapshot = new DonationSnapshotHistory();
        donationSnapshot.setOriginalDonationId(request.getDonationId());
        donationSnapshot.setDonationDate(request.getDonationDate());
        donationSnapshot.setStatus(DonationStatus.valueOf(request.getDonationStatus()));
        donationSnapshot.setBloodType(BloodType.valueOf(request.getBloodType()));
        donationSnapshot.setDonationType(DonationType.valueOf(request.getDonationType()));
        donationSnapshot.setQuantity(request.getQuantity());
        if (request.getOrganType() != null) {
            donationSnapshot.setOrganType(OrganType.valueOf(request.getOrganType()));
            donationSnapshot.setIsCompatible(request.getIsCompatible());
            donationSnapshot.setOrganQuality(request.getOrganQuality());
            donationSnapshot.setOrganViabilityExpiry(request.getOrganViabilityExpiry());
            donationSnapshot.setColdIschemiaTime(request.getColdIschemiaTime());
            donationSnapshot.setOrganPerfused(request.getOrganPerfused());
            donationSnapshot.setOrganWeight(request.getOrganWeight());
            donationSnapshot.setOrganSize(request.getOrganSize());
            donationSnapshot.setFunctionalAssessment(request.getFunctionalAssessment());
            donationSnapshot.setHasAbnormalities(request.getHasAbnormalities());
            donationSnapshot.setAbnormalityDescription(request.getAbnormalityDescription());
        }
        if (request.getTissueType() != null) {
            donationSnapshot.setTissueType(TissueType.valueOf(request.getTissueType()));
        }
        if (request.getStemCellType() != null) {
            donationSnapshot.setStemCellType(StemCellType.valueOf(request.getStemCellType()));
        }

        DonorHistory history = new DonorHistory();
        history.setDonorSnapshot(donorSnapshot);
        history.setMedicalDetailsSnapshot(medicalSnapshot);
        history.setEligibilityCriteriaSnapshot(eligibilitySnapshot);
        history.setHlaProfileSnapshot(hlaSnapshot);
        history.setConsentFormSnapshot(consentSnapshot);
        history.setDonationSnapshot(donationSnapshot);
        history.setMatchId(request.getMatchId());
        history.setReceiveRequestId(request.getReceiveRequestId());
        history.setRecipientUserId(request.getRecipientUserId());
        history.setMatchedAt(request.getMatchedAt());
        history.setCompletedAt(request.getCompletedAt());

        donorHistoryRepository.save(history);
    }
    
    @Override
    public List<DonorHistoryDTO> getDonorHistory(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) {
            throw new ResourceNotFoundException("Donor not found");
        }
        
        List<DonorHistory> histories = donorHistoryRepository.findByDonorSnapshot_UserId(userId);
        return histories.stream()
                .map(this::convertToHistoryDTO)
                .collect(Collectors.toList());
    }
    
    private DonorHistoryDTO convertToHistoryDTO(DonorHistory history) {
        DonorHistoryDTO dto = new DonorHistoryDTO();
        dto.setMatchId(history.getMatchId());
        dto.setReceiveRequestId(history.getReceiveRequestId());
        dto.setRecipientUserId(history.getRecipientUserId());
        dto.setMatchedAt(history.getMatchedAt());
        dto.setCompletedAt(history.getCompletedAt());
        
        if (history.getDonationSnapshot() != null) {
            DonationDTO donationDTO = new DonationDTO();
            donationDTO.setId(history.getDonationSnapshot().getOriginalDonationId());
            donationDTO.setDonationDate(history.getDonationSnapshot().getDonationDate());
            donationDTO.setStatus(history.getDonationSnapshot().getStatus());
            donationDTO.setBloodType(history.getDonationSnapshot().getBloodType());
            donationDTO.setDonationType(history.getDonationSnapshot().getDonationType());
            dto.setDonationSnapshot(donationDTO);
        }
        
        return dto;
    }

    private DonorDTO getDonorDTO(Donor savedDonor) {
        DonorDTO responseDTO = new DonorDTO();
        responseDTO.setId(savedDonor.getId());
        responseDTO.setUserId(savedDonor.getUserId());
        responseDTO.setRegistrationDate(savedDonor.getRegistrationDate());
        responseDTO.setStatus(savedDonor.getStatus());

        if (savedDonor.getMedicalDetails() != null) {
            MedicalDetailsDTO mdDTO = new MedicalDetailsDTO();
            BeanUtils.copyProperties(savedDonor.getMedicalDetails(), mdDTO);
            responseDTO.setMedicalDetails(mdDTO);
        }
        if (savedDonor.getEligibilityCriteria() != null) {
            EligibilityCriteriaDTO ecDTO = new EligibilityCriteriaDTO();
            BeanUtils.copyProperties(savedDonor.getEligibilityCriteria(), ecDTO);
            responseDTO.setEligibilityCriteria(ecDTO);
        }
        if (savedDonor.getConsentForm() != null) {
            ConsentFormDTO cfDTO = new ConsentFormDTO();
            BeanUtils.copyProperties(savedDonor.getConsentForm(), cfDTO);
            responseDTO.setConsentForm(cfDTO);
        }
        if (savedDonor.getAddresses() != null && !savedDonor.getAddresses().isEmpty()) {
            List<LocationDTO> locDTOList = savedDonor.getAddresses().stream().map(location -> {
                LocationDTO locDTO = new LocationDTO();
                BeanUtils.copyProperties(location, locDTO);
                return locDTO;
            }).collect(Collectors.toList());
            responseDTO.setAddresses(locDTOList);
        } else {
            responseDTO.setAddresses(new ArrayList<>());
        }
        if (savedDonor.getHlaProfile() != null) {
            HLAProfileDTO hlaProfileDTO = new HLAProfileDTO();
            BeanUtils.copyProperties(savedDonor.getHlaProfile(), hlaProfileDTO);
            responseDTO.setHlaProfile(hlaProfileDTO);
        }
        return responseDTO;
    }


    @Override
    public DonationDTO registerDonation(DonationRequestDTO donationRequestDTO) {
        Donor donor = donorRepository.findById(donationRequestDTO.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        validateDonorProfileComplete(donor);

        if (donationRequestDTO.getBloodType() == null) {
            throw new UnsupportedDonationTypeException("Blood type is required for all donation types.");
        }

        Location location = null;
        if (donationRequestDTO.getLocationId() != null) {
            location = locationRepository.findById(donationRequestDTO.getLocationId())
                    .orElseThrow(() -> new InvalidLocationException("Invalid location ID"));
        }
        System.out.println("DonationRequestDTO: " + donationRequestDTO);
        System.out.println("Location: " + location);

        Donation donation;
        switch (donationRequestDTO.getDonationType()) {
            case BLOOD -> {
                BloodDonation bloodDonation = new BloodDonation();
                bloodDonation.setDonor(donor);
                bloodDonation.setLocation(location);
                bloodDonation.setDonationDate(donationRequestDTO.getDonationDate());
                bloodDonation.setQuantity(donationRequestDTO.getQuantity());
                bloodDonation.setStatus(DonationStatus.PENDING);
                bloodDonation.setBloodType(donationRequestDTO.getBloodType());
                bloodDonation.setDonationType(DonationType.BLOOD);
                donation = bloodDonation;
            }
            case ORGAN -> {
                OrganDonation organDonation = new OrganDonation();
                organDonation.setDonor(donor);
                organDonation.setLocation(location);
                organDonation.setDonationDate(donationRequestDTO.getDonationDate());
                organDonation.setOrganType(donationRequestDTO.getOrganType());
                organDonation.setIsCompatible(Optional.ofNullable(donationRequestDTO.getIsCompatible()).orElse(false));
                organDonation.setStatus(DonationStatus.PENDING);
                organDonation.setBloodType(donationRequestDTO.getBloodType());
                organDonation.setDonationType(DonationType.ORGAN);

                organDonation.setOrganQuality(donationRequestDTO.getOrganQuality());
                organDonation.setOrganViabilityExpiry(donationRequestDTO.getOrganViabilityExpiry());
                organDonation.setColdIschemiaTime(donationRequestDTO.getColdIschemiaTime());
                organDonation.setOrganPerfused(donationRequestDTO.getOrganPerfused());
                organDonation.setOrganWeight(donationRequestDTO.getOrganWeight());
                organDonation.setOrganSize(donationRequestDTO.getOrganSize());
                organDonation.setFunctionalAssessment(donationRequestDTO.getFunctionalAssessment());
                organDonation.setHasAbnormalities(donationRequestDTO.getHasAbnormalities());
                organDonation.setAbnormalityDescription(donationRequestDTO.getAbnormalityDescription());

                donation = organDonation;
            }
            case TISSUE -> {
                TissueDonation tissueDonation = new TissueDonation();
                tissueDonation.setDonor(donor);
                tissueDonation.setLocation(location);
                tissueDonation.setDonationDate(donationRequestDTO.getDonationDate());
                tissueDonation.setTissueType(donationRequestDTO.getTissueType());
                tissueDonation.setQuantity(donationRequestDTO.getQuantity());
                tissueDonation.setStatus(DonationStatus.PENDING);
                tissueDonation.setBloodType(donationRequestDTO.getBloodType());
                tissueDonation.setDonationType(DonationType.TISSUE);
                donation = tissueDonation;
            }
            case STEM_CELL -> {
                StemCellDonation stemCellDonation = new StemCellDonation();
                stemCellDonation.setDonor(donor);
                stemCellDonation.setLocation(location);
                stemCellDonation.setDonationDate(donationRequestDTO.getDonationDate());
                stemCellDonation.setStemCellType(donationRequestDTO.getStemCellType());
                stemCellDonation.setQuantity(donationRequestDTO.getQuantity());
                stemCellDonation.setStatus(DonationStatus.PENDING);
                stemCellDonation.setBloodType(donationRequestDTO.getBloodType());
                stemCellDonation.setDonationType(DonationType.STEM_CELL);
                donation = stemCellDonation;
            }
            default -> throw new UnsupportedDonationTypeException(
                    "Donation type " + donationRequestDTO.getDonationType() + " is not supported.");
        }

        Donation savedDonation = donationRepository.save(donation);
        DonationDTO donationDTO = convertToDTO(savedDonation);

        eventPublisher.publishDonationEvent(getDonationEvent(donationDTO));
        eventPublisher.publishDonorEvent(getDonorEvent(donor));
        if (location != null) {
            eventPublisher.publishLocationEvent(getLocationEvent(location, donor.getId()));
        }
        HLAProfileEvent hlaProfileEvent = getHLAProfileEvent(donor);
        if (hlaProfileEvent != null) {
            eventPublisher.publishHLAProfileEvent(hlaProfileEvent);
        }
        System.out.println("Event published successfully.");
        return donationDTO;
    }

    private HLAProfileEvent getHLAProfileEvent(Donor donor) {
        if (donor == null || donor.getHlaProfile() == null) return null;

        HLAProfileEvent event = new HLAProfileEvent();
        HLAProfile hlaProfile = donor.getHlaProfile();

        BeanUtils.copyProperties(hlaProfile, event);
        event.setDonorId(donor.getId());

        return event;
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

    private LocationEvent getLocationEvent(Location location, UUID id) {
        if (location == null) return null;
        LocationEvent event = new LocationEvent();
        BeanUtils.copyProperties(location, event);
        event.setLocationId(location.getId());
        if (id != null) event.setDonorId(id);
        return event;
    }

    private DonorEvent getDonorEvent(Donor donor) {
        if (donor == null) return null;
        DonorEvent donorEvent = new DonorEvent();
        BeanUtils.copyProperties(donor, donorEvent);
        donorEvent.setDonorId(donor.getId());
        if (donor.getStatus() != null) donorEvent.setStatus(donor.getStatus().name());

        if (donor.getMedicalDetails() != null) {
            MedicalDetails md = donor.getMedicalDetails();
            donorEvent.setHemoglobinLevel(md.getHemoglobinLevel());
            donorEvent.setBloodPressure(md.getBloodPressure());
            donorEvent.setHasDiseases(md.getHasDiseases());
            donorEvent.setTakingMedication(md.getTakingMedication());
            donorEvent.setDiseaseDescription(md.getDiseaseDescription());

            donorEvent.setCurrentMedications(md.getCurrentMedications());
            donorEvent.setLastMedicalCheckup(md.getLastMedicalCheckup());
            donorEvent.setMedicalHistory(md.getMedicalHistory());
            donorEvent.setHasInfectiousDiseases(md.getHasInfectiousDiseases());
            donorEvent.setInfectiousDiseaseDetails(md.getInfectiousDiseaseDetails());
            donorEvent.setCreatinineLevel(md.getCreatinineLevel());
            donorEvent.setLiverFunctionTests(md.getLiverFunctionTests());
            donorEvent.setCardiacStatus(md.getCardiacStatus());
            donorEvent.setPulmonaryFunction(md.getPulmonaryFunction());
            donorEvent.setOverallHealthStatus(md.getOverallHealthStatus());
        }

        if (donor.getEligibilityCriteria() != null) {
            EligibilityCriteria ec = donor.getEligibilityCriteria();
            donorEvent.setMedicalClearance(ec.getMedicalClearance());
            donorEvent.setRecentSurgery(ec.getRecentSurgery());
            donorEvent.setChronicDiseases(ec.getChronicDiseases());
            donorEvent.setAllergies(ec.getAllergies());
            donorEvent.setLastDonationDate(ec.getLastDonationDate());
            donorEvent.setAge(ec.getAge());
            donorEvent.setWeight(ec.getWeight());
            donorEvent.setDob(ec.getDob());
            donorEvent.setRecentTattooOrPiercing(ec.getRecentTattooOrPiercing());
            donorEvent.setRecentTravelDetails(ec.getRecentTravelDetails());
            donorEvent.setRecentVaccination(ec.getRecentVaccination());
            donorEvent.setHeight(ec.getHeight());
            donorEvent.setBodyMassIndex(ec.getBodyMassIndex());
            donorEvent.setBodySize(ec.getBodySize());
            donorEvent.setIsLivingDonor(ec.getIsLivingDonor());
        }

        return donorEvent;
    }

    private DonationEvent getDonationEvent(DonationDTO donationDTO) {
        if (donationDTO == null) return null;
        DonationEvent event = new DonationEvent();
        event.setDonationId(donationDTO.getId());
        event.setDonorId(donationDTO.getDonorId());
        event.setLocationId(donationDTO.getLocationId());
        event.setDonationType(donationDTO.getDonationType());
        event.setBloodType(donationDTO.getBloodType());
        event.setDonationDate(donationDTO.getDonationDate());
        event.setStatus(donationDTO.getStatus());
        event.setQuantity(donationDTO.getQuantity());

        event.setOrganType(donationDTO.getOrganType());
        event.setIsCompatible(donationDTO.getIsCompatible());
        event.setOrganQuality(donationDTO.getOrganQuality());
        event.setOrganViabilityExpiry(donationDTO.getOrganViabilityExpiry());
        event.setColdIschemiaTime(donationDTO.getColdIschemiaTime());
        event.setOrganPerfused(donationDTO.getOrganPerfused());
        event.setOrganWeight(donationDTO.getOrganWeight());
        event.setOrganSize(donationDTO.getOrganSize());
        event.setFunctionalAssessment(donationDTO.getFunctionalAssessment());
        event.setHasAbnormalities(donationDTO.getHasAbnormalities());
        event.setAbnormalityDescription(donationDTO.getAbnormalityDescription());

        event.setTissueType(donationDTO.getTissueType());
        event.setStemCellType(donationDTO.getStemCellType());

        return event;
    }

    @Override
    public List<DonationDTO> getDonationsByDonorId(UUID donorId) {
        List<Donation> donations = donationRepository.findByDonorId(donorId);
        return donations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DonationDTO convertToDTO(Donation donation) {
        DonationDTO dto = new DonationDTO();
        dto.setId(donation.getId());
        dto.setDonorId(donation.getDonor().getId());
        dto.setLocationId(donation.getLocation() != null ? donation.getLocation().getId() : null);
        dto.setDonationDate(donation.getDonationDate());
        dto.setStatus(donation.getStatus());
        dto.setBloodType(donation.getBloodType());

        switch (donation) {
            case BloodDonation bloodDonation -> {
                dto.setDonationType(DonationType.BLOOD);
                dto.setQuantity(bloodDonation.getQuantity());
            }
            case OrganDonation organDonation -> {
                dto.setDonationType(DonationType.ORGAN);
                dto.setOrganType(organDonation.getOrganType());
                dto.setIsCompatible(organDonation.getIsCompatible());
                dto.setOrganQuality(organDonation.getOrganQuality());
                dto.setOrganViabilityExpiry(organDonation.getOrganViabilityExpiry());
                dto.setColdIschemiaTime(organDonation.getColdIschemiaTime());
                dto.setOrganPerfused(organDonation.getOrganPerfused());
                dto.setOrganWeight(organDonation.getOrganWeight());
                dto.setOrganSize(organDonation.getOrganSize());
                dto.setFunctionalAssessment(organDonation.getFunctionalAssessment());
                dto.setHasAbnormalities(organDonation.getHasAbnormalities());
                dto.setAbnormalityDescription(organDonation.getAbnormalityDescription());
            }
            case TissueDonation tissueDonation -> {
                dto.setDonationType(DonationType.TISSUE);
                dto.setTissueType(tissueDonation.getTissueType());
                dto.setQuantity(tissueDonation.getQuantity());
            }
            case StemCellDonation stemCellDonation -> {
                dto.setDonationType(DonationType.STEM_CELL);
                dto.setStemCellType(stemCellDonation.getStemCellType());
                dto.setQuantity(stemCellDonation.getQuantity());
            }
            default -> dto.setDonationType(null);
        }
        return dto;
    }

    private void validateDonorProfileComplete(Donor donor) {
        if (donor.getMedicalDetails() == null ||
                donor.getEligibilityCriteria() == null ||
                donor.getConsentForm() == null ||
                !Boolean.TRUE.equals(donor.getConsentForm().getIsConsented())) {
            throw new InvalidDonorProfileException(
                    "Donor profile is incomplete. Please complete all details including medical details, eligibility criteria, and consent form before donating."
            );
        }
    }
}
