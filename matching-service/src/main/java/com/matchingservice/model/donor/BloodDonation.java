package com.matchingservice.model.donor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "blood_donations")
public class BloodDonation extends Donation {

    @Column(nullable = false)
    private Double quantity;
}
