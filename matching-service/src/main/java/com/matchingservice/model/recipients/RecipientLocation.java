package com.matchingservice.model.recipients;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recipient_locations")
@Data
public class RecipientLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private UUID locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_db_id", referencedColumnName = "id")
    @JsonBackReference
    @ToString.Exclude
    private Recipient recipient;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String landmark;

    @Column(nullable = false)
    private String area;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String pincode;

    @Column(columnDefinition = "double precision")
    private Double latitude;

    @Column(columnDefinition = "double precision")
    private Double longitude;
}
