package com.QuoocCuongwf.EFEWallet.TransactionService.entity;

import com.QuoocCuongwf.EFEWallet.TransactionService.enums.TransactionType;
import com.QuoocCuongwf.EFEWallet.TransactionService.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID walletId; // ID của ví thực hiện giao dịch

    @Column(nullable = false)
    private UUID referenceId; // ID tham chiếu (ví dụ: OrderId từ MoMo hoặc ID ví người nhận)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal fee; // Phí giao dịch nếu có

    @Column(length = 500)
    private String description;

    @Column(unique = true)
    private String externalTransactionId; // Mã giao dịch từ đối tác (ví dụ: MoMo transactionId)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}