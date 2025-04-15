package com.donorservice.model;

import com.donorservice.enums.TissueType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tissue_donations")
public class TissueDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TissueType tissueType;

    @Column(nullable = false)
    private Double quantity;
}
