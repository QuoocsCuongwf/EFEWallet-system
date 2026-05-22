package com.QuoocCuongwf.EFEWallet.AuthService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MessageService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Object data) {
        kafkaTemplate.send(topic, data);
    }
}