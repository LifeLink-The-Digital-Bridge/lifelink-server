package com.matchingservice.model.donor;

import com.matchingservice.enums.TissueType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "tissue_donations")
public class TissueDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(name = "tissue_type")
    private TissueType tissueType;

    @Column
    private Double quantity;
}
