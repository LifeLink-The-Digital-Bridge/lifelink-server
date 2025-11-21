package com.notification.service;

import com.notification.client.DonorServiceClient;
import com.notification.client.RecipientServiceClient;
import com.notification.client.UserServiceClient;
import com.notification.dto.DonorDTO;
import com.notification.dto.RecipientDTO;
import com.notification.dto.UserDTO;
import com.notification.kafka.event.donor_events.*;
import com.notification.kafka.event.recipient_events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DonorServiceClient donorServiceClient;
    private final RecipientServiceClient recipientServiceClient;
    private final UserServiceClient userServiceClient;
    private final EmailService emailService;

    @Value("${internal.access-token:SECRET123456}")
    private String internalToken;

    public void processDonationEvent(DonationEvent event) {
        try {
            log.info("Processing donation event: {}", event.getDonationId());

            DonorDTO donor = donorServiceClient.getDonorById(event.getDonorId(), internalToken);
            UUID userId = donor.getUserId();
            log.info("Found donor userId: {}", userId);

            UserDTO user = userServiceClient.getUserById(userId, internalToken);
            String email = user.getEmail();
            String userName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
            log.info("Found user email: {}", email);

            emailService.sendDonationCreatedEmail(email, userName, event);
            log.info("Donation notification sent successfully for donationId: {}", event.getDonationId());

        } catch (Exception e) {
            log.error("Failed to process donation event: {}", event.getDonationId(), e);
        }
    }

    public void processReceiveRequestEvent(ReceiveRequestEvent event) {
        try {
            log.info("Processing receive request event: {}", event.getReceiveRequestId());

            RecipientDTO recipient = recipientServiceClient.getRecipientById(event.getRecipientId(), internalToken);
            UUID userId = recipient.getUserId();
            log.info("Found recipient userId: {}", userId);

            UserDTO user = userServiceClient.getUserById(userId, internalToken);
            String email = user.getEmail();
            String userName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
            log.info("Found user email: {}", email);

            emailService.sendRequestCreatedEmail(email, userName, event);
            log.info("Request notification sent successfully for requestId: {}", event.getReceiveRequestId());

        } catch (Exception e) {
            log.error("Failed to process receive request event: {}", event.getReceiveRequestId(), e);
        }
    }

    public void processDonationCancelledEvent(DonationCancelledEvent event) {
        try {
            log.info("Processing donation cancelled event: {}", event.getDonationId());

            UUID userId = event.getDonorUserId();
            log.info("Using donorUserId from event: {}", userId);

            UserDTO user = userServiceClient.getUserById(userId, internalToken);
            String email = user.getEmail();
            String userName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
            log.info("Found user email: {}", email);

            emailService.sendDonationCancelledEmail(email, userName, event);
            log.info("Donation cancellation notification sent successfully for donationId: {}", event.getDonationId());

        } catch (Exception e) {
            log.error("Failed to process donation cancelled event: {}", event.getDonationId(), e);
        }
    }

    public void processRequestCancelledEvent(RequestCancelledEvent event) {
        try {
            log.info("Processing request cancelled event: {}", event.getRequestId());

            UUID userId = event.getRecipientUserId();
            log.info("Using recipientUserId from event: {}", userId);

            UserDTO user = userServiceClient.getUserById(userId, internalToken);
            String email = user.getEmail();
            String userName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
            log.info("Found user email: {}", email);

            emailService.sendRequestCancelledEmail(email, userName, event);
            log.info("Request cancellation notification sent successfully for requestId: {}", event.getRequestId());

        } catch (Exception e) {
            log.error("Failed to process request cancelled event: {}", event.getRequestId(), e);
        }
    }
}
