package com.matchingservice.configuration;

import com.matchingservice.kafka.event.donor_events.*;
import com.matchingservice.kafka.event.recipient_events.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

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
    public ConsumerFactory<String, DonorHLAProfileEvent> hlaProfileConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DonorHLAProfileEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonorHLAProfileEvent> hlaProfileKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, DonorHLAProfileEvent>();
        factory.setConsumerFactory(hlaProfileConsumerFactory());
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

    @Bean
    public ConsumerFactory<String, RecipientHLAProfileEvent> recipientHlaProfileConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RecipientHLAProfileEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RecipientHLAProfileEvent> recipientHlaProfileKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RecipientHLAProfileEvent>();
        factory.setConsumerFactory(recipientHlaProfileConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DonationCancelledEvent> donationCancelledConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DonationCancelledEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonationCancelledEvent> donationCancelledKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, DonationCancelledEvent>();
        factory.setConsumerFactory(donationCancelledConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, RequestCancelledEvent> requestCancelledConsumerFactory() {
        Map<String, Object> props = new HashMap<>(baseConsumerProps());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RequestCancelledEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RequestCancelledEvent> requestCancelledKafkaListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RequestCancelledEvent>();
        factory.setConsumerFactory(requestCancelledConsumerFactory());
        return factory;
    }
}
