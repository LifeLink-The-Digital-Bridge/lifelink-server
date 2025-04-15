package com.donorservice.service;

import com.donorservice.client.UserClient;
import com.donorservice.dto.DonorDTO;
import com.donorservice.dto.RegisterDonor;
import com.donorservice.dto.UserDTO;
import com.donorservice.exception.ResourceNotFoundException;
import com.donorservice.model.Donor;
import com.donorservice.repository.DonorRepository;
import com.donorservice.service.DonorService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DonorServiceImpl implements DonorService {

    private final DonorRepository donorRepository;
    private final UserClient userClient;

    public DonorServiceImpl(DonorRepository donorRepository, UserClient userClient) {
        this.donorRepository = donorRepository;
        this.userClient = userClient;
    }

    @Override
    public DonorDTO createDonor(RegisterDonor donorDTO) {
        UserDTO userDTO;
        try {
            userDTO = userClient.getUserById(donorDTO.getUserId());
            if (userDTO == null) {
                throw new ResourceNotFoundException("User not found with ID: " + donorDTO.getUserId());
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found with ID: " + donorDTO.getUserId());
        }

        Donor donor = new Donor();
        BeanUtils.copyProperties(donorDTO, donor);
        donor.setUserId(donorDTO.getUserId());

        Donor savedDonor = donorRepository.save(donor);

        DonorDTO responseDTO = new DonorDTO();
        BeanUtils.copyProperties(savedDonor, responseDTO);
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
}
