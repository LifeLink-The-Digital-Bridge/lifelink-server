package com.notification.service;

import com.notification.client.DonorServiceClient;
import com.notification.client.RecipientServiceClient;
import com.notification.client.UserServiceClient;
import com.notification.dto.DonorDTO;
import com.notification.dto.NotificationDTO;
import com.notification.dto.RecipientDTO;
import com.notification.dto.UserDTO;
import com.notification.kafka.event.donor_events.*;
import com.notification.kafka.event.recipient_events.*;
import com.notification.websocket.NotificationWebSocketHandler;
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
    private final InAppNotificationService inAppNotificationService;
    private final NotificationWebSocketHandler webSocketHandler;

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
            log.info("Email notification sent for donationId: {}", event.getDonationId());

            NotificationDTO notification = inAppNotificationService.createDonationNotification(event, userId);
            log.info("In-app notification created: {}", notification.getId());

            webSocketHandler.sendNotificationToUser(userId, notification);

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
            log.info("Email notification sent for requestId: {}", event.getReceiveRequestId());

            NotificationDTO notification = inAppNotificationService.createRequestNotification(event, userId);
            log.info("In-app notification created: {}", notification.getId());

            webSocketHandler.sendNotificationToUser(userId, notification);

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
            log.info("Email notification sent for donationId: {}", event.getDonationId());

            NotificationDTO notification = inAppNotificationService.createDonationCancelledNotification(event);
            log.info("In-app notification created: {}", notification.getId());

            webSocketHandler.sendNotificationToUser(userId, notification);

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
            log.info("Email notification sent for requestId: {}", event.getRequestId());

            NotificationDTO notification = inAppNotificationService.createRequestCancelledNotification(event);
            log.info("In-app notification created: {}", notification.getId());

            webSocketHandler.sendNotificationToUser(userId, notification);

        } catch (Exception e) {
            log.error("Failed to process request cancelled event: {}", event.getRequestId(), e);
        }
    }

    public void processMatchFoundEvent(com.notification.kafka.event.MatchFoundEvent event) {
        try {
            log.info("Processing match found event: {}", event.getMatchId());

            UserDTO donorUser = userServiceClient.getUserById(event.getDonorUserId(), internalToken);
            String donorEmail = donorUser.getEmail();
            String donorName = donorUser.getFirstName() != null ? donorUser.getFirstName() : donorUser.getUsername();
            String donorProfileLink = "/profile/" + donorUser.getUsername();

            UserDTO recipientUser = userServiceClient.getUserById(event.getRecipientUserId(), internalToken);
            String recipientEmail = recipientUser.getEmail();
            String recipientName = recipientUser.getFirstName() != null ? recipientUser.getFirstName() : recipientUser.getUsername();
            String recipientProfileLink = "/profile/" + recipientUser.getUsername();

            com.notification.dto.DonationDTO donation = donorServiceClient.getDonationById(event.getDonationId(), internalToken);
            com.notification.dto.ReceiveRequestDTO request = recipientServiceClient.getReceiveRequestById(event.getReceiveRequestId(), internalToken);

            log.info("Match found between Donor: {} and Recipient: {}", donorUser.getUsername(), recipientUser.getUsername());

            emailService.sendMatchFoundEmail(donorEmail, donorName, recipientName, recipientProfileLink, event, true, donation, request);
            
            emailService.sendMatchFoundEmail(recipientEmail, recipientName, donorName, donorProfileLink, event, false, donation, request);

            NotificationDTO donorNotification = inAppNotificationService.createMatchFoundNotification(event, event.getDonorUserId(), recipientName, recipientUser.getUsername(), donation, request);
            webSocketHandler.sendNotificationToUser(event.getDonorUserId(), donorNotification);

            NotificationDTO recipientNotification = inAppNotificationService.createMatchFoundNotification(event, event.getRecipientUserId(), donorName, donorUser.getUsername(), donation, request);
            webSocketHandler.sendNotificationToUser(event.getRecipientUserId(), recipientNotification);

            log.info("Match notifications sent successfully for matchId: {}", event.getMatchId());

        } catch (Exception e) {
            log.error("Failed to process match found event: {}", event.getMatchId(), e);
        }
    }
}
