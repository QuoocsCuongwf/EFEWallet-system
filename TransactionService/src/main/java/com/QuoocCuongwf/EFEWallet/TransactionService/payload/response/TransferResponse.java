package com.QuoocCuongwf.EFEWallet.TransactionService.payload.response;

import com.QuoocCuongwf.EFEWallet.TransactionService.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {
    private UUID TransactionId;
    private TransactionStatus status;
    private BigDecimal amount;
    private  UUID fromWalletId;
    private  UUID toWalletId;
    private LocalDateTime createAt;
}
