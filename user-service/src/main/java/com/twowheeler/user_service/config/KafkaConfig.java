package com.twowheeler.user_service.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;


@Configuration
@EnableKafka
public class KafkaConfig {

    private static final String BOOTSTRAP_SERVERS = "kafka:9092";

    /* ---------------- PRODUCER ---------------- */

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); // ✅ VERY IMPORTANT
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /* ---------------- CONSUMER ---------------- */

   
   @Bean
public ConsumerFactory<String, Object> consumerFactory() {

    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // 🔥 CRITICAL LINES
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
            "com.twowheeler.user_service.kafka.BaseEvent");

    ErrorHandlingDeserializer<String> keyDeserializer =
            new ErrorHandlingDeserializer<>(new StringDeserializer());

    ErrorHandlingDeserializer<Object> valueDeserializer =
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>());

    return new DefaultKafkaConsumerFactory<>(
            props,
            keyDeserializer,
            valueDeserializer
    );
}

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        return factory;
    }
}
