package com.QuoocCuongwf.EFEWallet.TransactionService.entity;

import com.QuoocCuongwf.EFEWallet.TransactionService.enums.TransactionStatus;
import com.QuoocCuongwf.EFEWallet.TransactionService.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "idemotencyKey")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    private String requestHash;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Enumerated(EnumType.STRING)
    private TransactionType resourceType;
    private String resourceId;   // transactionId

    private LocalDateTime createdAt;
}
