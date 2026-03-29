package com.QuoocCuongwf.EFEWallet.TransactionService.payload.request;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @NonNull
    private UUID toWalletId;
    @Min(1)
    private BigDecimal mount;
    private String description;
    private String referenceCode;
}
