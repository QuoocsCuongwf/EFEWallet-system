package com.QuoocsCuongwf.EFEWallet.WalletService.payload.response;

import jakarta.persistence.PrePersist;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BalanceResponse {
    private String walletAddress;
    private BigDecimal balance;
    private LocalDateTime time;

    @PrePersist
    public void prePersist(){
        this.time=LocalDateTime.now();
    }
}
