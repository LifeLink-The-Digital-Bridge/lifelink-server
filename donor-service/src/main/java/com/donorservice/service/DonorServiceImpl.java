package com.donorservice.service;

import com.donorservice.client.UserClient;
import com.donorservice.client.UserGrpcClient;
import com.donorservice.dto.*;
import com.donorservice.enums.*;
import com.donorservice.exception.*;
import com.donorservice.kafka.EventPublisher;
import com.donorservice.kafka.event.*;
import com.donorservice.model.*;
import com.donorservice.repository.*;
import com.userservice.grpc.UserProfileResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;
    private final LocationRepository locationRepository;
    private final EventPublisher eventPublisher;
    private final ProfileLockService profileLockService;
    private final UserGrpcClient userGrpcClient;

    public DonorServiceImpl(DonorRepository donorRepository, DonationRepository donationRepository, LocationRepository locationRepository, EventPublisher eventPublisher, ProfileLockService profileLockService, UserGrpcClient userGrpcClient) {
        this.donorRepository = donorRepository;
        this.donationRepository = donationRepository;
        this.locationRepository = locationRepository;
        this.eventPublisher = eventPublisher;
        this.profileLockService = profileLockService;
        this.userGrpcClient = userGrpcClient;
    }

    @Override
    public DonorDTO saveOrUpdateDonor(UUID userId, RegisterDonor donorDTO) {
        Donor donor = donorRepository.findByUserId(userId);

        if (donor != null && profileLockService.isDonorProfileLocked(donor.getId())) {
            throw new IllegalStateException(profileLockService.getProfileLockReason(donor.getId()));
        }

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
                    Location existing = locationRepository.findById(locDTO.getId()).orElseThrow(() -> new InvalidLocationException("Address not found"));
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
        Donor savedDonor = donorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Donor with id " + id + " not found!"));
        return getDonorDTO(savedDonor);
    }

    @Override
    public List<DonationDTO> getDonationsByUserId(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) {
            throw new ResourceNotFoundException("Donor not found");
        }
        return getDonationsByDonorId(donor.getId(), userId);
    }

    @Override
    public DonorDTO getDonorByUserId(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) throw new ResourceNotFoundException("Donor not found");
        return getDonorDTO(donor);
    }

    @Override
    public void updateDonationStatus(UUID donationId, DonationStatus status) {
        donationRepository.findById(donationId).ifPresent(donation -> {
            donation.setStatus(status);
            donationRepository.save(donation);
        });
    }

    @Override
    public String getDonationStatus(UUID donationId) {
        return donationRepository.findById(donationId).map(donation -> donation.getStatus().toString()).orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
    }

    @Override
    public DonationDTO getDonationById(UUID donationId) {
        Donation donation = donationRepository.findById(donationId).orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
        return convertToDTO(donation);
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
            mdDTO.setDonorId(savedDonor.getId());
            responseDTO.setMedicalDetails(mdDTO);
        }

        if (savedDonor.getEligibilityCriteria() != null) {
            EligibilityCriteriaDTO ecDTO = new EligibilityCriteriaDTO();
            BeanUtils.copyProperties(savedDonor.getEligibilityCriteria(), ecDTO);
            ecDTO.setDonorId(savedDonor.getId());
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
        Donor donor = donorRepository.findById(donationRequestDTO.getDonorId()).orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        validateDonorProfileComplete(donor, donationRequestDTO.getDonationType());

        if (donationRequestDTO.getBloodType() == null) {
            throw new UnsupportedDonationTypeException("Blood type is required for all donation types.");
        }

        Location location = null;
        if (donationRequestDTO.getLocationId() != null) {
            location = locationRepository.findById(donationRequestDTO.getLocationId()).orElseThrow(() -> new InvalidLocationException("Invalid location ID"));
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
            default ->
                    throw new UnsupportedDonationTypeException("Donation type " + donationRequestDTO.getDonationType() + " is not supported.");
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
            System.out.println("HLA profile event published for " + donationRequestDTO.getDonationType() + " donation");
        } else {
            System.out.println("No HLA profile to publish for " + donationRequestDTO.getDonationType() + " donation");
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
        if (locDTO.getAddressLine() == null || locDTO.getLandmark() == null || locDTO.getArea() == null || locDTO.getCity() == null || locDTO.getDistrict() == null || locDTO.getState() == null || locDTO.getCountry() == null || locDTO.getPincode() == null || locDTO.getLatitude() == null || locDTO.getLongitude() == null) {
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
            donorEvent.setMedicalDetailsId(md.getId());
            donorEvent.setHemoglobinLevel(md.getHemoglobinLevel());
            donorEvent.setBloodGlucoseLevel(md.getBloodGlucoseLevel());
            donorEvent.setHasDiabetes(md.getHasDiabetes());
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
            donorEvent.setEligibilityCriteriaId(ec.getId());
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

            if (ec.getSmokingStatus() != null) {
                donorEvent.setSmokingStatus(ec.getSmokingStatus());
            }
            donorEvent.setPackYears(ec.getPackYears());
            donorEvent.setQuitSmokingDate(ec.getQuitSmokingDate());

            if (ec.getAlcoholStatus() != null) {
                donorEvent.setAlcoholStatus(ec.getAlcoholStatus());
            }
            donorEvent.setDrinksPerWeek(ec.getDrinksPerWeek());
            donorEvent.setQuitAlcoholDate(ec.getQuitAlcoholDate());
            donorEvent.setAlcoholAbstinenceMonths(ec.getAlcoholAbstinenceMonths());
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
    public List<DonationDTO> getDonationsByDonorId(UUID donorId, UUID requesterId) {
        Donor donor = donorRepository.findById(donorId).orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        UUID donorUserId = donor.getUserId();

        if (donorUserId.equals(requesterId)) {
            List<Donation> donations = donationRepository.findByDonorId(donorId);
            return donations.stream().map(this::convertToDTO).collect(Collectors.toList());
        }

        UserProfileResponse userProfile = userGrpcClient.getUserProfile(donorUserId);
        Visibility visibility = Visibility.valueOf(userProfile.getProfileVisibility());

        if (visibility == Visibility.PRIVATE) {
            throw new AccessDeniedException("This user's donations are private");
        }

        if (visibility == Visibility.FOLLOWERS_ONLY) {
            boolean isFollowing = userGrpcClient.checkFollowStatus(requesterId, donorUserId);
            if (!isFollowing) {
                throw new AccessDeniedException("You must follow this user to view their donations");
            }
        }

        List<Donation> donations = donationRepository.findByDonorId(donorId);
        return donations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CancellationResponseDTO cancelDonation(UUID donationId, UUID userId, CancellationRequestDTO request) {
        Donation donation = donationRepository.findById(donationId).orElseThrow(() -> new ResourceNotFoundException("Donation not found with ID: " + donationId));

        if (!donation.getDonor().getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own donations");
        }

        if (donation.getStatus() == DonationStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel completed donation");
        }

        if (donation.getStatus() == DonationStatus.CANCELLED_BY_DONOR) {
            throw new InvalidOperationException("Donation is already cancelled");
        }

        if (donation.getStatus() == DonationStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Donation is in progress. Please contact support at support@lifelink.com " + "or call 1-800-LIFELINK for cancellation assistance.");
        }

        donation.setStatus(DonationStatus.CANCELLED_BY_DONOR);
        donation.setCancellationReason(request.getReason());
        donation.setAdditionalCancellationNotes(request.getAdditionalNotes());
        donation.setCancelledAt(LocalDateTime.now());
        donation.setCancelledByUserId(userId);

        Donation savedDonation = donationRepository.save(donation);

        profileLockService.releaseLock(donation.getDonor().getId());
        boolean profileUnlocked = !profileLockService.isDonorProfileLocked(donation.getDonor().getId());

        DonationCancelledEvent event = DonationCancelledEvent.builder().donationId(donationId).donorId(donation.getDonor().getId()).donorUserId(userId).cancellationReason(request.getReason()).cancelledAt(LocalDateTime.now()).eventType("DONATION_CANCELLED").build();

        try {
            eventPublisher.publishDonationCancelledEvent(event);
            System.out.println("Published cancellation event for donation: " + donationId);
        } catch (Exception e) {
            System.err.println("Failed to publish cancellation event: " + e.getMessage());
        }

        return CancellationResponseDTO.builder().success(true).message("Donation cancelled successfully. All pending matches will be expired.").donationId(donationId).cancelledAt(savedDonation.getCancelledAt()).cancellationReason(savedDonation.getCancellationReason()).expiredMatchesCount(0).profileUnlocked(profileUnlocked).build();
    }

    @Override
    public ProfileLockInfoDTO getProfileLockInfo(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) {
            throw new ResourceNotFoundException("Donor not found for user: " + userId);
        }

        return profileLockService.getDetailedLockInfo(donor.getId());
    }

    @Override
    public boolean canCancelDonation(UUID donationId, UUID userId) {
        try {
            Donation donation = donationRepository.findById(donationId).orElse(null);

            if (donation == null) {
                return false;
            }

            if (!donation.getDonor().getUserId().equals(userId)) {
                return false;
            }

            return donation.getStatus() != DonationStatus.COMPLETED && donation.getStatus() != DonationStatus.CANCELLED_BY_DONOR && donation.getStatus() != DonationStatus.IN_PROGRESS;

        } catch (Exception e) {
            return false;
        }
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

    private void validateDonorProfileComplete(Donor donor, DonationType donationType) {
        if (donor.getMedicalDetails() == null || donor.getEligibilityCriteria() == null || donor.getConsentForm() == null || !Boolean.TRUE.equals(donor.getConsentForm().getIsConsented())) {
            throw new InvalidDonorProfileException("Donor profile is incomplete. Please complete all details.");
        }
        validateHLAProfileRequired(donationType, donor);
    }

    private void validateHLAProfileRequired(DonationType donationType, Donor donor) {
        if ((donationType == DonationType.ORGAN || donationType == DonationType.TISSUE || donationType == DonationType.STEM_CELL) && donor.getHlaProfile() == null) {
            throw new IncompleteProfileException("HLA profile is required for " + donationType + " donation. " + "Please complete your HLA typing before registering this donation type.");
        }
    }


}
