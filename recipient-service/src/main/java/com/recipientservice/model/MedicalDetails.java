package com.recipientservice.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipient_medical_details")
public class MedicalDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;

    @Column
    private String diagnosis;

    @Column
    private String allergies;

    @Column
    private String currentMedications;

    @Column
    private String additionalNotes;
}

