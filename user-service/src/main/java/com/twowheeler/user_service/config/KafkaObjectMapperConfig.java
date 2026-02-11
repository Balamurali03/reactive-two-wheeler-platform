package com.twowheeler.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaObjectMapperConfig {

    @Bean
    public ObjectMapper kafkaObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()   // handles Java time, etc.
                .build();
    }
}
