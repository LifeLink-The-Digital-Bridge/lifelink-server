package com.recipientservice.model;

import com.recipientservice.dto.User;
import com.recipientservice.enums.Availability;
import com.recipientservice.enums.BloodType;
import com.recipientservice.enums.UrgencyLevel;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false) // Store userId instead of User object
    private Long userId;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_blood_type")
    private BloodType requiredBloodType;

    @Column(name = "organ_needed")
    private String organNeeded;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level")
    private UrgencyLevel urgencyLevel;

    @Transient // Not stored in DB, used for API response
    private User user;

    public Recipient() {
        this.availability = Availability.AVAILABLE;
    }

    // Getters and setters
}
