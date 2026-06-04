package com.QuoocsCuongwf.EFEWallet.WalletService.payload.KafkaMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TransferMessage {
    private String walletSend;
    private String walletRecive;
    private BigDecimal amount;
    private LocalDateTime time;
    private String description;
}
