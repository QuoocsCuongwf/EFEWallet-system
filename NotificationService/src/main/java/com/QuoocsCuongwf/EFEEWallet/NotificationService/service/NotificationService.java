package com.QuoocsCuongwf.EFEEWallet.NotificationService.service;

import com.QuoocsCuongwf.EFEEWallet.NotificationService.dto.TransferMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    private KafkaTemplate<Object,Object> kafkaTemplate;
    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "notification-group"
    )
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
    @KafkaListener(topics = "wallet-transactions", groupId = "notification-group")
    public void consumeTransactionEvent(TransferMessage message) {
        log.info("🔔 [NOTIFICATION EVENT] Nhận thành công gói tin giao dịch: {}", message.getTransactionId());
        log.info("Ví gửi: {} -> Ví nhận: {} | Số tiền: {} EFE",
                message.getWalletSend(), message.getWalletRecive(), message.getAmount());
    }
}
