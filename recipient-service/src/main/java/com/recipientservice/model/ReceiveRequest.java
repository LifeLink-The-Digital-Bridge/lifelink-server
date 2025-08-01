package com.recipientservice.model;

import com.recipientservice.enums.BloodType;
import com.recipientservice.enums.OrganType;
import com.recipientservice.enums.UrgencyLevel;
import com.recipientservice.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "receive_requests")
public class ReceiveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;

    @Enumerated(EnumType.STRING)
    private BloodType requestedBloodType;

    @Enumerated(EnumType.STRING)
    private OrganType requestedOrgan;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column
    private String notes;
}
