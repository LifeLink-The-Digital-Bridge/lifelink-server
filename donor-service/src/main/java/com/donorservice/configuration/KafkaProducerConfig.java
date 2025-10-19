package com.donorservice.configuration;

import com.donorservice.kafka.event.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ProducerFactory<String, DonorEvent> donorProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DonorEvent> donorKafkaTemplate() {
        return new KafkaTemplate<>(donorProducerFactory());
    }

    @Bean
    public ProducerFactory<String, DonationEvent> donationProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DonationEvent> donationKafkaTemplate() {
        return new KafkaTemplate<>(donationProducerFactory());
    }

    @Bean
    public ProducerFactory<String, LocationEvent> locationProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, LocationEvent> locationKafkaTemplate() {
        return new KafkaTemplate<>(locationProducerFactory());
    }

    @Bean
    public ProducerFactory<String, HLAProfileEvent> hlaProfileProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, HLAProfileEvent> hlaProfileKafkaTemplate() {
        return new KafkaTemplate<>(hlaProfileProducerFactory());
    }

    @Bean
    public ProducerFactory<String, DonationCancelledEvent> donationCancelledProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, DonationCancelledEvent> donationCancelledKafkaTemplate() {
        return new KafkaTemplate<>(donationCancelledProducerFactory());
    }
}
