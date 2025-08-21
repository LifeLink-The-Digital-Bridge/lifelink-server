package com.donorservice.configuration;

import com.donorservice.kafka.event.DonorEvent;
import com.donorservice.kafka.event.DonationEvent;
import com.donorservice.kafka.event.HLAProfileEvent;
import com.donorservice.kafka.event.LocationEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.*;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, DonorEvent> donorProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, DonorEvent> donorKafkaTemplate() {
        return new KafkaTemplate<>(donorProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, DonationEvent> donationKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(donorProducerFactory().getConfigurationProperties()));
    }

    @Bean
    public KafkaTemplate<String, LocationEvent> locationKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(donorProducerFactory().getConfigurationProperties()));
    }

    @Bean KafkaTemplate<String, HLAProfileEvent> hlaProfileKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(donorProducerFactory().getConfigurationProperties()));
    }
}
