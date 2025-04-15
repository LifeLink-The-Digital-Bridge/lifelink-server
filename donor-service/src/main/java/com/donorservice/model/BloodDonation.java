package com.donorservice.model;

import com.donorservice.enums.BloodType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "blood_donations")
public class BloodDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Column(nullable = false)
    private Double quantity;
}
