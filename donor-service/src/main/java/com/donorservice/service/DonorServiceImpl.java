package com.donorservice.service;

import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.enums.DonationType;
import com.donorservice.exception.InvalidDonorProfileException;
import com.donorservice.exception.InvalidLocationException;
import com.donorservice.exception.ResourceNotFoundException;
import com.donorservice.exception.UnsupportedDonationTypeException;
import com.donorservice.kafka.EventPublisher;
import com.donorservice.kafka.event.DonationEvent;
import com.donorservice.kafka.event.DonorEvent;
import com.donorservice.kafka.event.LocationEvent;
import com.donorservice.model.*;
import com.donorservice.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;
    private final LocationRepository locationRepository;
    private final EventPublisher eventPublisher;

    public DonorServiceImpl(DonorRepository donorRepository, DonationRepository donationRepository, UserClient userClient, LocationRepository locationRepository, EventPublisher eventPublisher) {
        this.donorRepository = donorRepository;
        this.donationRepository = donationRepository;
        this.locationRepository = locationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DonorDTO createDonor(UUID userId, RegisterDonor donorDTO) {
        Donor donor = donorRepository.findByUserId(userId);
        if (donor == null) {
            donor = new Donor();
            donor.setUserId(userId);
            BeanUtils.copyProperties(donorDTO, donor);
        } else {
            donor.setRegistrationDate(donorDTO.getRegistrationDate());
            donor.setStatus(donorDTO.getStatus());
        }

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
        donor.setConsentForm(consentForm);

        if (donorDTO.getLocation() != null) {
            Location location = donor.getLocation();
            if (location == null) {
                location = new Location();
            } else {
                donorDTO.getLocation().setId(location.getId());
            }

            LocationDTO locDTO = donorDTO.getLocation();
            if (locDTO.getAddressLine() == null || locDTO.getLandmark() == null || locDTO.getArea() == null ||
                    locDTO.getCity() == null || locDTO.getDistrict() == null || locDTO.getState() == null ||
                    locDTO.getCountry() == null || locDTO.getPincode() == null ||
                    locDTO.getLatitude() == null || locDTO.getLongitude() == null) {
                throw new InvalidLocationException("All location fields must be provided and non-null.");
            }

            BeanUtils.copyProperties(locDTO, location);
            location = locationRepository.save(location);
            donor.setLocation(location);
        }


        Donor savedDonor = donorRepository.save(donor);
        return getDonorDTO(savedDonor);
    }


    @Override
    public DonorDTO getDonorById(UUID id) {
        Donor savedDonor = donorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Donor with id " + id + " not found!"));
        return getDonorDTO(savedDonor);
    }

    @Override
    public DonorDTO getDonorByUserId(UUID userId) {
        Donor donor = donorRepository.findByUserId(userId);
        return getDonorDTO(donor);
    }

    private DonorDTO getDonorDTO(Donor savedDonor) {
        DonorDTO responseDTO = new DonorDTO();
        BeanUtils.copyProperties(savedDonor, responseDTO);

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
        if (savedDonor.getLocation() != null) {
            LocationDTO locDTO = new LocationDTO();
            BeanUtils.copyProperties(savedDonor.getLocation(), locDTO);
            responseDTO.setLocation(locDTO);
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
                    .orElse(null);
        }

        Donation donation;
        switch (donationRequestDTO.getDonationType()) {
            case BLOOD:
                BloodDonation bloodDonation = new BloodDonation();
                bloodDonation.setDonor(donor);
                bloodDonation.setLocation(location);
                bloodDonation.setDonationDate(donationRequestDTO.getDonationDate());
                bloodDonation.setQuantity(donationRequestDTO.getQuantity());
                bloodDonation.setStatus(donationRequestDTO.getStatus() != null ? donationRequestDTO.getStatus() : "PENDING");
                bloodDonation.setBloodType(donationRequestDTO.getBloodType());
                donation = bloodDonation;
                break;
            case ORGAN:
                OrganDonation organDonation = new OrganDonation();
                organDonation.setDonor(donor);
                organDonation.setLocation(location);
                organDonation.setDonationDate(donationRequestDTO.getDonationDate());
                organDonation.setOrganType(donationRequestDTO.getOrganType());
                organDonation.setIsCompatible(donationRequestDTO.getIsCompatible() != null ? donationRequestDTO.getIsCompatible() : false);
                organDonation.setStatus(donationRequestDTO.getStatus() != null ? donationRequestDTO.getStatus() : "PENDING");
                organDonation.setBloodType(donationRequestDTO.getBloodType());
                donation = organDonation;
                break;
            case TISSUE:
                TissueDonation tissueDonation = new TissueDonation();
                tissueDonation.setDonor(donor);
                tissueDonation.setLocation(location);
                tissueDonation.setDonationDate(donationRequestDTO.getDonationDate());
                tissueDonation.setTissueType(donationRequestDTO.getTissueType());
                tissueDonation.setQuantity(donationRequestDTO.getQuantity());
                tissueDonation.setStatus(donationRequestDTO.getStatus() != null ? donationRequestDTO.getStatus() : "PENDING");
                tissueDonation.setBloodType(donationRequestDTO.getBloodType());
                donation = tissueDonation;
                break;
            case STEM_CELL:
                StemCellDonation stemCellDonation = new StemCellDonation();
                stemCellDonation.setDonor(donor);
                stemCellDonation.setLocation(location);
                stemCellDonation.setDonationDate(donationRequestDTO.getDonationDate());
                stemCellDonation.setStemCellType(donationRequestDTO.getStemCellType());
                stemCellDonation.setQuantity(donationRequestDTO.getQuantity());
                stemCellDonation.setStatus(donationRequestDTO.getStatus() != null ? donationRequestDTO.getStatus() : "PENDING");
                stemCellDonation.setBloodType(donationRequestDTO.getBloodType());
                donation = stemCellDonation;
                break;
            default:
                throw new UnsupportedDonationTypeException("Donation type " + donationRequestDTO.getDonationType() + " is not supported.");
        }

        Donation savedDonation = donationRepository.save(donation);

        System.out.println(savedDonation);
        System.out.println(savedDonation.getBloodType());

        DonationDTO donationDTO = new DonationDTO();
        donationDTO.setId(savedDonation.getId());
        donationDTO.setDonorId(savedDonation.getDonor().getId());
        donationDTO.setBloodType(savedDonation.getBloodType());
        System.out.println(donationDTO.getBloodType());
        donationDTO.setLocationId(savedDonation.getLocation() != null ? savedDonation.getLocation().getId() : null);
        donationDTO.setDonationType(donationRequestDTO.getDonationType());
        donationDTO.setDonationDate(savedDonation.getDonationDate());
        donationDTO.setStatus(savedDonation.getStatus());

        switch (donationRequestDTO.getDonationType()) {
            case BLOOD:
                BloodDonation bd = (BloodDonation) savedDonation;
                donationDTO.setBloodType(bd.getBloodType());
                donationDTO.setQuantity(bd.getQuantity());
                break;
            case ORGAN:
                OrganDonation od = (OrganDonation) savedDonation;
                donationDTO.setOrganType(od.getOrganType());
                donationDTO.setIsCompatible(od.getIsCompatible());
                break;
            case TISSUE:
                TissueDonation td = (TissueDonation) savedDonation;
                donationDTO.setTissueType(td.getTissueType());
                donationDTO.setQuantity(td.getQuantity());
                break;
            case STEM_CELL:
                StemCellDonation sd = (StemCellDonation) savedDonation;
                donationDTO.setStemCellType(sd.getStemCellType());
                donationDTO.setQuantity(sd.getQuantity());
                break;
            default:
                break;
        }
        eventPublisher.publishDonationEvent(getDonationEvent(donationDTO));
        eventPublisher.publishDonorEvent(getDonorEvent(donor));
        eventPublisher.publishLocationEvent(getLocationEvent(donor.getLocation(), donor.getId()));
        return donationDTO;
    }

    private LocationEvent getLocationEvent(Location location, UUID id) {
        if (location == null) {
            return null;
        }
        LocationEvent event = new LocationEvent();
        BeanUtils.copyProperties(location, event);
        event.setLocationId(location.getId());
        if (id != null) {
            event.setDonorId(id);
        }
        return event;
    }

    private DonorEvent getDonorEvent(Donor donor) {
        if (donor == null) {
            return null;
        }
        DonorEvent donorEvent = new DonorEvent();

        BeanUtils.copyProperties(donor, donorEvent);

        donorEvent.setDonorId(donor.getId());
        if (donor.getStatus() != null) {
            donorEvent.setStatus(donor.getStatus().name());
        }

        if (donor.getMedicalDetails() != null) {
            donorEvent.setHemoglobinLevel(donor.getMedicalDetails().getHemoglobinLevel());
            donorEvent.setBloodPressure(donor.getMedicalDetails().getBloodPressure());
            donorEvent.setHasDiseases(donor.getMedicalDetails().getHasDiseases());
            donorEvent.setTakingMedication(donor.getMedicalDetails().getTakingMedication());
            donorEvent.setDiseaseDescription(donor.getMedicalDetails().getDiseaseDescription());
        }

        if (donor.getEligibilityCriteria() != null) {
            donorEvent.setMedicalClearance(donor.getEligibilityCriteria().getMedicalClearance());
            donorEvent.setRecentSurgery(donor.getEligibilityCriteria().getRecentSurgery());
            donorEvent.setChronicDiseases(donor.getEligibilityCriteria().getChronicDiseases());
            donorEvent.setAllergies(donor.getEligibilityCriteria().getAllergies());
            donorEvent.setLastDonationDate(donor.getEligibilityCriteria().getLastDonationDate());
            donorEvent.setAge(donor.getEligibilityCriteria().getAge());
            donorEvent.setWeight(donor.getEligibilityCriteria().getWeight());
        }

        return donorEvent;
    }

    public static DonationEvent getDonationEvent(DonationDTO donationDTO) {
        if (donationDTO == null) {
            return null;
        }

        DonationEvent event = new DonationEvent();

        event.setDonationId(donationDTO.getId());
        event.setDonorId(donationDTO.getDonorId());
        event.setLocationId(donationDTO.getLocationId());
        event.setDonationType(donationDTO.getDonationType());

        event.setBloodType(donationDTO.getBloodType() != null ? donationDTO.getBloodType().name() : null);

        event.setDonationDate(donationDTO.getDonationDate());
        event.setQuantity(donationDTO.getQuantity());
        event.setOrganType(donationDTO.getOrganType());
        event.setIsCompatible(donationDTO.getIsCompatible());
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
            throw new InvalidDonorProfileException("Donor profile is incomplete. Please complete all details including medical details, eligibility criteria, and consent form before donating.");
        }
    }

}
