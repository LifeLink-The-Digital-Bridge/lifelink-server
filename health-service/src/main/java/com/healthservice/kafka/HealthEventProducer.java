package com.healthservice.kafka;

import com.healthservice.dto.HealthRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String HEALTH_RECORD_TOPIC = "health-record-events";
    private static final String EMERGENCY_HEALTH_TOPIC = "emergency-health-events";

    public void publishHealthEvent(String topic, Map<String, Object> eventData) {
        try {
            Object key = eventData.get("userId");
            if (key == null) {
                key = eventData.get("doctorId");
            }
            if (key == null) {
                key = eventData.get("ngoId");
            }
            kafkaTemplate.send(topic, key != null ? key.toString() : null, eventData);
            log.info("Published health event to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to publish health event to topic: {}", topic, e);
        }
    }

    public void publishHealthRecordEvent(HealthRecordDTO healthRecord) {
        try {
            kafkaTemplate.send(HEALTH_RECORD_TOPIC, healthRecord.getUserId().toString(), healthRecord);
            log.info("Published health record event for user: {}", healthRecord.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish health record event", e);
        }
    }

    public void publishEmergencyHealthEvent(HealthRecordDTO healthRecord) {
        try {
            kafkaTemplate.send(EMERGENCY_HEALTH_TOPIC, healthRecord.getUserId().toString(), healthRecord);
            log.info("Published emergency health event for user: {}", healthRecord.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish emergency health event", e);
        }
    }
}
