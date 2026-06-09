package com.QuoocsCuongwf.EFEEWallet.NotificationService.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import tools.jackson.databind.ObjectMapper;


@Configuration
@EnableKafka
public class KafkaConfig {


}