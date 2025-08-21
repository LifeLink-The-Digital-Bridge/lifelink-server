package com.matchingservice.kafka.event.recipient_events;

import com.matchingservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ReceiveRequestEvent {
    private UUID receiveRequestId;
    private UUID recipientId;
    private UUID locationId;
    private RequestType requestType;
    private BloodType requestedBloodType;
    private OrganType requestedOrgan;
    private TissueType requestedTissue;
    private StemCellType requestedStemCellType;
    private UrgencyLevel urgencyLevel;
    private Double quantity;
    private LocalDate requestDate;
    private RequestStatus status;
    private String notes;
}
