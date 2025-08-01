package com.recipientservice.kafka.events;

import com.recipientservice.enums.BloodType;
import com.recipientservice.enums.OrganType;
import com.recipientservice.enums.UrgencyLevel;
import com.recipientservice.enums.RequestStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ReceiveRequestEvent {
    private UUID receiveRequestId;
    private UUID recipientId;
    private BloodType requestedBloodType;
    private OrganType requestedOrgan;
    private UrgencyLevel urgencyLevel;
    private Double quantity;
    private LocalDate requestDate;
    private RequestStatus status;
    private String notes;
}
