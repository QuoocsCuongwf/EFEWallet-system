package com.QuoocsCuongwf.EFEEWallet.NotificationService.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private KafkaTemplate<Object,Object> kafkaTemplate;
    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "notification-group"
    )
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}
