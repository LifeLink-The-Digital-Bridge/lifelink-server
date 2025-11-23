package com.notification.dto;

import com.notification.enums.RequestType;
import com.notification.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveRequestDTO {
    private UUID receiveRequestId;
    private UUID recipientId;
    private UUID userId;
    private RequestType requestType;
    private String requestedBloodType;
    private String requestedOrgan;
    private String requestedTissue;
    private String requestedStemCellType;
    private UrgencyLevel urgencyLevel;
    private LocalDate requestDate;
    private String status;
}
