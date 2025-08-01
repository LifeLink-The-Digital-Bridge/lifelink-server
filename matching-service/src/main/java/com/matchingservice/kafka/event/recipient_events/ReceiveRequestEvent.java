package com.matchingservice.kafka.event.recipient_events;

import com.matchingservice.enums.BloodType;
import com.matchingservice.enums.OrganType;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.enums.UrgencyLevel;
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
