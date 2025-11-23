package com.notification.kafka;

import com.notification.kafka.event.donor_events.*;
import com.notification.kafka.event.recipient_events.*;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "donation-events",
            groupId = "notification-service-group",
            containerFactory = "donationKafkaListenerFactory"
    )
    public void consumeDonationEvent(DonationEvent event) {
        log.info("Received DonationEvent: {}", event.getDonationId());
        notificationService.processDonationEvent(event);
    }

    @KafkaListener(
            topics = "receive-request-events",
            groupId = "notification-service-group",
            containerFactory = "receiveRequestKafkaListenerFactory"
    )
    public void consumeReceiveRequestEvent(ReceiveRequestEvent event) {
        log.info("Received ReceiveRequestEvent: {}", event.getReceiveRequestId());
        notificationService.processReceiveRequestEvent(event);
    }

    @KafkaListener(
            topics = "donation-cancelled",
            groupId = "notification-service-group",
            containerFactory = "donationCancelledKafkaListenerFactory"
    )
    public void consumeDonationCancelledEvent(DonationCancelledEvent event) {
        log.info("Received DonationCancelledEvent: {}", event.getDonationId());
        notificationService.processDonationCancelledEvent(event);
    }

    @KafkaListener(
            topics = "request-cancelled",
            groupId = "notification-service-group",
            containerFactory = "requestCancelledKafkaListenerFactory"
    )
    public void consumeRequestCancelledEvent(RequestCancelledEvent event) {
        log.info("Received RequestCancelledEvent: {}", event.getRequestId());
        notificationService.processRequestCancelledEvent(event);
    }

    @KafkaListener(
            topics = "match-found-events",
            groupId = "notification-service-group",
            containerFactory = "matchFoundListenerFactory"
    )
    public void consumeMatchFoundEvent(com.notification.kafka.event.MatchFoundEvent event) {
        log.info("Received MatchFoundEvent: {}", event.getMatchId());
        notificationService.processMatchFoundEvent(event);
    }
}
