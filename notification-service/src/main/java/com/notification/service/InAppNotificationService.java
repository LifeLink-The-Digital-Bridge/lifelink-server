package com.notification.service;

import com.notification.dto.NotificationDTO;
import com.notification.enums.NotificationType;
import com.notification.kafka.event.donor_events.DonationCancelledEvent;
import com.notification.kafka.event.donor_events.DonationEvent;
import com.notification.kafka.event.recipient_events.ReceiveRequestEvent;
import com.notification.kafka.event.recipient_events.RequestCancelledEvent;
import com.notification.model.Notification;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDTO createNotification(UUID userId, NotificationType type, String title, 
                                             String message, Map<String, Object> metadata) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .metadata(metadata)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification: {} for user: {}", saved.getId(), userId);

        return mapToDTO(saved);
    }

    public NotificationDTO createDonationNotification(DonationEvent event, UUID userId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("donationId", event.getDonationId().toString());
        metadata.put("donationType", event.getDonationType().toString());
        metadata.put("donationDate", event.getDonationDate().toString());

        String title = "Donation Registered Successfully";
        String message = buildDonationMessage(event);

        return createNotification(userId, NotificationType.DONATION_CREATED, title, message, metadata);
    }

    private String buildDonationMessage(DonationEvent event) {
        String specificType = getSpecificDonationType(event);
        return String.format("Your %s %s donation has been successfully registered on %s. " +
                "We will notify you once a match is found.", 
                specificType, event.getDonationType(), event.getDonationDate());
    }

    private String getSpecificDonationType(DonationEvent event) {
        switch (event.getDonationType()) {
            case BLOOD:
                return event.getBloodType() != null ? formatBloodType(event.getBloodType().toString()) : "";
            case ORGAN:
                return event.getOrganType() != null ? formatEnumName(event.getOrganType().toString()) : "";
            case TISSUE:
                return event.getTissueType() != null ? formatEnumName(event.getTissueType().toString()) : "";
            case STEM_CELL:
                return event.getStemCellType() != null ? formatEnumName(event.getStemCellType().toString()) : "";
            default:
                return "";
        }
    }

    public NotificationDTO createRequestNotification(ReceiveRequestEvent event, UUID userId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestId", event.getReceiveRequestId().toString());
        metadata.put("requestType", event.getRequestType().toString());
        metadata.put("urgencyLevel", event.getUrgencyLevel().toString());
        metadata.put("requestDate", event.getRequestDate().toString());

        String title = "Request Registered Successfully";
        String message = buildRequestMessage(event);

        return createNotification(userId, NotificationType.REQUEST_CREATED, title, message, metadata);
    }

    private String buildRequestMessage(ReceiveRequestEvent event) {
        String specificType = getSpecificRequestType(event);
        return String.format("Your %s %s request (Urgency: %s) has been successfully registered. " +
                "We are actively searching for a suitable match.", 
                specificType, event.getRequestType(), event.getUrgencyLevel());
    }

    private String getSpecificRequestType(ReceiveRequestEvent event) {
        switch (event.getRequestType()) {
            case BLOOD:
                return event.getRequestedBloodType() != null ? formatBloodType(event.getRequestedBloodType().toString()) : "";
            case ORGAN:
                return event.getRequestedOrgan() != null ? formatEnumName(event.getRequestedOrgan().toString()) : "";
            case TISSUE:
                return event.getRequestedTissue() != null ? formatEnumName(event.getRequestedTissue().toString()) : "";
            case STEM_CELL:
                return event.getRequestedStemCellType() != null ? formatEnumName(event.getRequestedStemCellType().toString()) : "";
            default:
                return "";
        }
    }

    private String formatBloodType(String bloodType) {
        return bloodType.replace("_POSITIVE", "+")
                       .replace("_NEGATIVE", "-");
    }

    private String formatEnumName(String name) {
        if (name == null || name.isEmpty()) return "";
        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    public NotificationDTO createDonationCancelledNotification(DonationCancelledEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("donationId", event.getDonationId().toString());
        metadata.put("cancelledAt", event.getCancelledAt().toString());
        if (event.getDonationType() != null) {
            metadata.put("donationType", event.getDonationType().toString());
        }

        String title = "Donation Cancelled";
        String message = buildDonationCancelledMessage(event);

        return createNotification(event.getDonorUserId(), NotificationType.DONATION_CANCELLED, 
                title, message, metadata);
    }

    private String buildDonationCancelledMessage(DonationCancelledEvent event) {
        String specificType = "";
        if (event.getDonationType() != null) {
            specificType = getSpecificDonationTypeFromCancelled(event) + " ";
        }
        return String.format("Your %s%s donation has been cancelled. Reason: %s", 
                specificType, 
                event.getDonationType() != null ? event.getDonationType().toString() : "",
                event.getCancellationReason());
    }

    private String getSpecificDonationTypeFromCancelled(DonationCancelledEvent event) {
        switch (event.getDonationType()) {
            case BLOOD:
                return event.getBloodType() != null ? formatBloodType(event.getBloodType().toString()) : "";
            case ORGAN:
                return event.getOrganType() != null ? formatEnumName(event.getOrganType().toString()) : "";
            case TISSUE:
                return event.getTissueType() != null ? formatEnumName(event.getTissueType().toString()) : "";
            case STEM_CELL:
                return event.getStemCellType() != null ? formatEnumName(event.getStemCellType().toString()) : "";
            default:
                return "";
        }
    }

    public NotificationDTO createRequestCancelledNotification(RequestCancelledEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestId", event.getRequestId().toString());
        metadata.put("cancelledAt", event.getCancelledAt().toString());
        if (event.getRequestType() != null) {
            metadata.put("requestType", event.getRequestType().toString());
        }

        String title = "Request Cancelled";
        String message = buildRequestCancelledMessage(event);

        return createNotification(event.getRecipientUserId(), NotificationType.REQUEST_CANCELLED, 
                title, message, metadata);
    }

    private String buildRequestCancelledMessage(RequestCancelledEvent event) {
        String specificType = "";
        if (event.getRequestType() != null) {
            specificType = getSpecificRequestTypeFromCancelled(event) + " ";
        }
        return String.format("Your %s%s request has been cancelled. Reason: %s", 
                specificType,
                event.getRequestType() != null ? event.getRequestType().toString() : "",
                event.getCancellationReason());
    }

    private String getSpecificRequestTypeFromCancelled(RequestCancelledEvent event) {
        switch (event.getRequestType()) {
            case BLOOD:
                return event.getRequestedBloodType() != null ? formatBloodType(event.getRequestedBloodType().toString()) : "";
            case ORGAN:
                return event.getRequestedOrgan() != null ? formatEnumName(event.getRequestedOrgan().toString()) : "";
            case TISSUE:
                return event.getRequestedTissue() != null ? formatEnumName(event.getRequestedTissue().toString()) : "";
            case STEM_CELL:
                return event.getRequestedStemCellType() != null ? formatEnumName(event.getRequestedStemCellType().toString()) : "";
            default:
                return "";
        }
    }

    public List<NotificationDTO> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public NotificationDTO markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        log.info("Marked notification {} as read for user {}", notificationId, userId);

        return mapToDTO(updated);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);

        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked all notifications as read for user {}", userId);
    }

   @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notificationRepository.delete(notification);
        log.info("Deleted notification {} for user {}", notificationId, userId);
    }

    public NotificationDTO createMatchFoundNotification(com.notification.kafka.event.MatchFoundEvent event, UUID userId, 
                                                        String otherUserName, String otherUserUsername,
                                                        com.notification.dto.DonationDTO donation, 
                                                        com.notification.dto.ReceiveRequestDTO request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("matchId", event.getMatchId().toString());
        metadata.put("matchedAt", event.getMatchedAt().toString());
        metadata.put("otherUserName", otherUserName);
        metadata.put("otherUserUsername", otherUserUsername);
        metadata.put("compatibilityScore", String.format("%.1f%%", event.getCompatibilityScore() * 100));

        String specificType = "";
        String itemType = "";

        boolean isDonor = userId.equals(event.getDonorUserId());
        
        if (isDonor && donation != null) {
            itemType = "donation";
            if (donation.getDonationType() != null) {
                switch (donation.getDonationType()) {
                    case BLOOD:
                        specificType = donation.getBloodType() != null ? formatBloodType(donation.getBloodType()) : "";
                        break;
                    case ORGAN:
                        specificType = donation.getOrganType() != null ? formatEnumName(donation.getOrganType()) : "";
                        break;
                    case TISSUE:
                        specificType = donation.getTissueType() != null ? formatEnumName(donation.getTissueType()) : "";
                        break;
                    case STEM_CELL:
                        specificType = donation.getStemCellType() != null ? formatEnumName(donation.getStemCellType()) : "";
                        break;
                }
                itemType = donation.getDonationType().toString() + " " + itemType;
            }
        } else if (!isDonor && request != null) {
            itemType = "request";
            if (request.getRequestType() != null) {
                switch (request.getRequestType()) {
                    case BLOOD:
                        specificType = request.getRequestedBloodType() != null ? formatBloodType(request.getRequestedBloodType()) : "";
                        break;
                    case ORGAN:
                        specificType = request.getRequestedOrgan() != null ? formatEnumName(request.getRequestedOrgan()) : "";
                        break;
                    case TISSUE:
                        specificType = request.getRequestedTissue() != null ? formatEnumName(request.getRequestedTissue()) : "";
                        break;
                    case STEM_CELL:
                        specificType = request.getRequestedStemCellType() != null ? formatEnumName(request.getRequestedStemCellType()) : "";
                        break;
                }
                itemType = request.getRequestType().toString() + " " + itemType;
            }
        }

        String typeDescription = (specificType.isEmpty() ? "" : specificType + " ") + itemType;

        String title = "Match Found!";
        String message = String.format("Match found for your %s! Matched with %s (Score: %.1f%%). Click to view details.", 
                typeDescription, otherUserName, event.getCompatibilityScore() * 100);

        return createNotification(userId, NotificationType.MATCH_FOUND, title, message, metadata);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .metadata(notification.getMetadata())
                .build();
    }
}
