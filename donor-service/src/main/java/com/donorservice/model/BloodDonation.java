package com.donorservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "blood_donations")
public class BloodDonation extends Donation {

    @Column(nullable = false)
    private Double quantity;
}
