package com.recipientservice.configuration;

import com.recipientservice.kafka.events.*;
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
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    @Bean
    public ProducerFactory<String, RecipientEvent> recipientProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, RecipientEvent> recipientKafkaTemplate() {
        return new KafkaTemplate<>(recipientProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ReceiveRequestEvent> receiveRequestProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, ReceiveRequestEvent> receiveRequestKafkaTemplate() {
        return new KafkaTemplate<>(receiveRequestProducerFactory());
    }

    @Bean
    public ProducerFactory<String, LocationEvent> locationProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, LocationEvent> recipientLocationKafkaTemplate() {
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
    public ProducerFactory<String, RequestCancelledEvent> requestCancelledProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, RequestCancelledEvent> requestCancelledKafkaTemplate() {
        return new KafkaTemplate<>(requestCancelledProducerFactory());
    }
}
