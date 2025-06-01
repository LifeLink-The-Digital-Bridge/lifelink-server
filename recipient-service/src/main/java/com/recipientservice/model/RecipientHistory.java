package com.recipientservice.model;

import com.recipientservice.enums.BloodType;
import com.recipientservice.enums.OrganType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_history")
public class RecipientHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;

    @Column(nullable = false)
    private UUID donorId;

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    private OrganType organType;

    @Column
    private Double quantity;

    @Column
    private String location;

}
