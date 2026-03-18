package com.QuoocCuongwf.EFEWallet.TransactionService.payload.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private UUID toWalletId;
    private BigDecimal mount;
    private String description;
    private String referenceCode;
}
