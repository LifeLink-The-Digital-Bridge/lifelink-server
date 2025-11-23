package com.matchingservice.kafka;

import com.matchingservice.kafka.event.MatchFoundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.match-found-events:match-found-events}")
    private String matchFoundTopic;

    public void publishMatchFoundEvent(MatchFoundEvent event) {
        try {
            log.info("Publishing match found event for matchId: {}", event.getMatchId());
            kafkaTemplate.send(matchFoundTopic, event.getMatchId().toString(), event);
            log.info("Successfully published match found event");
        } catch (Exception e) {
            log.error("Error publishing match found event: {}", e.getMessage(), e);
        }
    }
}
