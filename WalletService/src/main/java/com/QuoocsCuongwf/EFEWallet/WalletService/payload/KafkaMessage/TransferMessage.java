package com.QuoocsCuongwf.EFEWallet.WalletService.payload.KafkaMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferMessage {
    private String emailSend;
    private String emailRecive;
    private BigDecimal amount;
    private LocalDateTime time;
    private String description;
}
