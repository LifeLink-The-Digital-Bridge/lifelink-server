package com.donorservice.model;

import com.donorservice.enums.OrganType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "organ_donations")
public class OrganDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganType organType;

    @Column(nullable = false)
    private Boolean isCompatible;
}
