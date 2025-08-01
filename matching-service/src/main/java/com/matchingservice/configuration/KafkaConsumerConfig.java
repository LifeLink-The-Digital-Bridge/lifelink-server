package com.matchingservice.configuration;

import com.matchingservice.kafka.event.donor_events.*;
import com.matchingservice.kafka.event.recipient_events.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private final String bootstrapServers = "localhost:9092";

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "matching-service-group");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ConsumerFactory<String, DonorEvent> donorConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DonorEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonorEvent> donorKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, DonorEvent>();
        factory.setConsumerFactory(donorConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DonationEvent> donationConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DonationEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonationEvent> donationKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, DonationEvent>();
        factory.setConsumerFactory(donationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DonorLocationEvent> donorLocationConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DonorLocationEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonorLocationEvent> donorLocationKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, DonorLocationEvent>();
        factory.setConsumerFactory(donorLocationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, RecipientEvent> recipientConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RecipientEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RecipientEvent> recipientKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RecipientEvent>();
        factory.setConsumerFactory(recipientConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ReceiveRequestEvent> receiveRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ReceiveRequestEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReceiveRequestEvent> receiveRequestKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ReceiveRequestEvent>();
        factory.setConsumerFactory(receiveRequestConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, RecipientLocationEvent> recipientLocationConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RecipientLocationEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RecipientLocationEvent> recipientLocationKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RecipientLocationEvent>();
        factory.setConsumerFactory(recipientLocationConsumerFactory());
        return factory;
    }
}
