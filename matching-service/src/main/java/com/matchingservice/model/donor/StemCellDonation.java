package com.matchingservice.model.donor;

import com.matchingservice.enums.StemCellType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "stem_cell_donations")
public class StemCellDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(name = "stem_cell_type")
    private StemCellType stemCellType;

    @Column
    private Double quantity;
}
