package com.donorservice.service;

import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.enums.DonationType;
import com.donorservice.exception.InvalidDonorProfileException;
import com.donorservice.exception.InvalidLocationException;
import com.donorservice.exception.ResourceNotFoundException;
import com.donorservice.exception.UnsupportedDonationTypeException;
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

    public DonorServiceImpl(DonorRepository donorRepository, DonationRepository donationRepository, UserClient userClient, LocationRepository locationRepository) {
        this.donorRepository = donorRepository;
        this.donationRepository = donationRepository;
        this.locationRepository = locationRepository;
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
    public DonationDTO registerDonation(DonationRequestDTO donationDTO) {
        Donor donor = donorRepository.findById(donationDTO.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        validateDonorProfileComplete(donor);

        if (donationDTO.getBloodType() == null) {
            throw new UnsupportedDonationTypeException("Blood type is required for all donation types.");
        }

        Location location = null;
        if (donationDTO.getLocationId() != null) {
            location = locationRepository.findById(donationDTO.getLocationId())
                    .orElse(null);
        }

        Donation donation;
        switch (donationDTO.getDonationType()) {
            case BLOOD:
                BloodDonation bloodDonation = new BloodDonation();
                bloodDonation.setDonor(donor);
                bloodDonation.setLocation(location);
                bloodDonation.setDonationDate(donationDTO.getDonationDate());
                bloodDonation.setQuantity(donationDTO.getQuantity());
                bloodDonation.setStatus(donationDTO.getStatus() != null ? donationDTO.getStatus() : "PENDING");
                bloodDonation.setBloodType(donationDTO.getBloodType());
                donation = bloodDonation;
                break;
            case ORGAN:
                OrganDonation organDonation = new OrganDonation();
                organDonation.setDonor(donor);
                organDonation.setLocation(location);
                organDonation.setDonationDate(donationDTO.getDonationDate());
                organDonation.setOrganType(donationDTO.getOrganType());
                organDonation.setIsCompatible(donationDTO.getIsCompatible() != null ? donationDTO.getIsCompatible() : false);
                organDonation.setStatus(donationDTO.getStatus() != null ? donationDTO.getStatus() : "PENDING");
                organDonation.setBloodType(donationDTO.getBloodType());
                donation = organDonation;
                break;
            case TISSUE:
                TissueDonation tissueDonation = new TissueDonation();
                tissueDonation.setDonor(donor);
                tissueDonation.setLocation(location);
                tissueDonation.setDonationDate(donationDTO.getDonationDate());
                tissueDonation.setTissueType(donationDTO.getTissueType());
                tissueDonation.setQuantity(donationDTO.getQuantity());
                tissueDonation.setStatus(donationDTO.getStatus() != null ? donationDTO.getStatus() : "PENDING");
                tissueDonation.setBloodType(donationDTO.getBloodType());
                donation = tissueDonation;
                break;
            case STEM_CELL:
                StemCellDonation stemCellDonation = new StemCellDonation();
                stemCellDonation.setDonor(donor);
                stemCellDonation.setLocation(location);
                stemCellDonation.setDonationDate(donationDTO.getDonationDate());
                stemCellDonation.setStemCellType(donationDTO.getStemCellType());
                stemCellDonation.setQuantity(donationDTO.getQuantity());
                stemCellDonation.setStatus(donationDTO.getStatus() != null ? donationDTO.getStatus() : "PENDING");
                stemCellDonation.setBloodType(donationDTO.getBloodType());
                donation = stemCellDonation;
                break;
            default:
                throw new UnsupportedDonationTypeException("Donation type " + donationDTO.getDonationType() + " is not supported.");
        }

        Donation savedDonation = donationRepository.save(donation);

        System.out.println(savedDonation);
        System.out.println(savedDonation.getBloodType());

        DonationDTO dto = new DonationDTO();
        dto.setId(savedDonation.getId());
        dto.setDonorId(savedDonation.getDonor().getId());
        dto.setBloodType(savedDonation.getBloodType());
        System.out.println(dto.getBloodType());
        dto.setLocationId(savedDonation.getLocation() != null ? savedDonation.getLocation().getId() : null);
        dto.setDonationType(donationDTO.getDonationType());
        dto.setDonationDate(savedDonation.getDonationDate());
        dto.setStatus(savedDonation.getStatus());

        switch (donationDTO.getDonationType()) {
            case BLOOD:
                BloodDonation bd = (BloodDonation) savedDonation;
                dto.setBloodType(bd.getBloodType());
                dto.setQuantity(bd.getQuantity());
                break;
            case ORGAN:
                OrganDonation od = (OrganDonation) savedDonation;
                dto.setOrganType(od.getOrganType());
                dto.setIsCompatible(od.getIsCompatible());
                break;
            case TISSUE:
                TissueDonation td = (TissueDonation) savedDonation;
                dto.setTissueType(td.getTissueType());
                dto.setQuantity(td.getQuantity());
                break;
            case STEM_CELL:
                StemCellDonation sd = (StemCellDonation) savedDonation;
                dto.setStemCellType(sd.getStemCellType());
                dto.setQuantity(sd.getQuantity());
                break;
            default:
                break;
        }
        return dto;
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
