package com.donorservice.service;

import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.enums.DonationType;
import com.donorservice.exception.ResourceNotFoundException;
import com.donorservice.model.*;
import com.donorservice.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        Optional<Donor> existingDonorOpt = donorRepository.findByUserId(userId);

        Donor donor;
        if (existingDonorOpt.isPresent()) {
            donor = existingDonorOpt.get();

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
            donor.setConsentForm(consentForm);

            if (donorDTO.getLocation() != null) {
                Location location = donor.getLocation();
                if (location == null) {
                    location = new Location();
                } else {
                    donorDTO.getLocation().setId(location.getId());
                }
                BeanUtils.copyProperties(donorDTO.getLocation(), location);
                location = locationRepository.save(location);
                donor.setLocation(location);
            }
        } else {
            donor = new Donor();
            BeanUtils.copyProperties(donorDTO, donor);

            MedicalDetails medicalDetails = new MedicalDetails();
            BeanUtils.copyProperties(donorDTO.getMedicalDetails(), medicalDetails);
            medicalDetails.setDonor(donor);
            donor.setMedicalDetails(medicalDetails);

            EligibilityCriteria eligibilityCriteria = new EligibilityCriteria();
            BeanUtils.copyProperties(donorDTO.getEligibilityCriteria(), eligibilityCriteria);
            eligibilityCriteria.setDonor(donor);
            donor.setEligibilityCriteria(eligibilityCriteria);

            ConsentForm consentForm = new ConsentForm();
            BeanUtils.copyProperties(donorDTO.getConsentForm(), consentForm);
            consentForm.setDonor(donor);
            donor.setConsentForm(consentForm);

            if (donorDTO.getLocation() != null) {
                Location location = new Location();
                BeanUtils.copyProperties(donorDTO.getLocation(), location);
                location = locationRepository.save(location);
                donor.setLocation(location);
            }
            donor.setUserId(userId);
        }

        Donor savedDonor = donorRepository.save(donor);

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
    public DonorDTO getDonorById(UUID id) {
        Optional<Donor> donor = donorRepository.findById(id);
        if (donor.isPresent()) {
            DonorDTO donorDTO = new DonorDTO();
            BeanUtils.copyProperties(donor.get(), donorDTO);
            return donorDTO;
        }
        throw new ResourceNotFoundException("Donor not found with ID: " + id);
    }

    @Override
    public DonationDTO registerDonation(DonationRequestDTO donationDTO) {
        Donor donor = donorRepository.findById(donationDTO.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));

        validateDonorProfileComplete(donor);
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
                bloodDonation.setBloodType(donationDTO.getBloodType());
                bloodDonation.setStatus(donationDTO.getStatus() != null ? donationDTO.getStatus() : "PENDING");
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
                donation = stemCellDonation;
                break;
            default:
                throw new IllegalArgumentException("Unsupported donation type");
        }
        donation.setBloodType(donationDTO.getBloodType());
        donationRepository.save(donation);

        DonationDTO dto = new DonationDTO();
        dto.setId(donation.getId());
        dto.setDonorId(donor.getId());
        dto.setBloodType(donation.getBloodType());
        dto.setLocationId(location != null ? location.getId() : null);
        dto.setDonationType(donationDTO.getDonationType());
        dto.setDonationDate(donation.getDonationDate());
        dto.setStatus(donation.getStatus());

        switch (donationDTO.getDonationType()) {
            case BLOOD:
                BloodDonation bd = (BloodDonation) donation;
                dto.setBloodType(bd.getBloodType());
                dto.setQuantity(bd.getQuantity());
                break;
            case ORGAN:
                OrganDonation od = (OrganDonation) donation;
                dto.setOrganType(od.getOrganType());
                dto.setIsCompatible(od.getIsCompatible());
                break;
            case TISSUE:
                TissueDonation td = (TissueDonation) donation;
                dto.setTissueType(td.getTissueType());
                dto.setQuantity(td.getQuantity());
                break;
            case STEM_CELL:
                StemCellDonation sd = (StemCellDonation) donation;
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

        if (donation instanceof BloodDonation) {
            dto.setDonationType(DonationType.BLOOD);
            dto.setQuantity(((BloodDonation) donation).getQuantity());
        } else if (donation instanceof OrganDonation) {
            dto.setDonationType(DonationType.ORGAN);
            dto.setOrganType(((OrganDonation) donation).getOrganType());
            dto.setIsCompatible(((OrganDonation) donation).getIsCompatible());
        } else if (donation instanceof TissueDonation) {
            dto.setDonationType(DonationType.TISSUE);
            dto.setTissueType(((TissueDonation) donation).getTissueType());
            dto.setQuantity(((TissueDonation) donation).getQuantity());
        } else if (donation instanceof StemCellDonation) {
            dto.setDonationType(DonationType.STEM_CELL);
            dto.setStemCellType(((StemCellDonation) donation).getStemCellType());
            dto.setQuantity(((StemCellDonation) donation).getQuantity());
        } else {
            dto.setDonationType(null);
        }

        return dto;
    }

    private void validateDonorProfileComplete(Donor donor) {
        if (donor.getMedicalDetails() == null ||
                donor.getEligibilityCriteria() == null ||
                donor.getConsentForm() == null ||
                !Boolean.TRUE.equals(donor.getConsentForm().getIsConsented())) {
            throw new IllegalStateException("Donor profile is incomplete. Please complete all details before donating.");
        }
    }

}
