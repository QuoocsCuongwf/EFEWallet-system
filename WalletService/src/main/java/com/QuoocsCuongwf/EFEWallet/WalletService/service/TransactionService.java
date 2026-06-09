package com.QuoocsCuongwf.EFEWallet.WalletService.service;

import com.QuoocsCuongwf.EFEWallet.WalletService.Enum.WalletStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.TransactionEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.KafkaMessage.TransferMessage;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.request.TransferRequest;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.TransferResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.util.SecurityUtils;
import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.*;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepnsitory;
    private final WalletRepository walletRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey) {

        UUID fromUserId = SecurityUtils.getCurrentUserId();
        WalletEntity toWallet = walletRepository.findByWalletAddressForUpdate(request.getToWalletAddress(), WalletStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        UUID toUserId = toWallet.getUserId();

        WalletEntity fromWallet = walletRepository.findByUserIdForUpdate(fromUserId,WalletStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("Error Wallet tranfer not activate"));

        TransactionEntity transaction = TransactionEntity.builder()
                .fromWallet(fromWallet.getWalletAddress())
                .fromUserId(fromUserId)
                .toWallet(toWallet.getWalletAddress())
                .toUserId(toUserId)
                .idempotencyKey(idempotencyKey)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        transaction = transactionRepnsitory.save(transaction);

        try {

            Boolean isDedit = walletService.debit(
                    request.getAmount(),
                    fromUserId
            );
            Boolean isCredit = walletService.credit(
                    request.getAmount(),
                    request.getToWalletAddress()
            );
            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepnsitory.save(transaction);
            TransferMessage transferMessage = TransferMessage.builder()
                    .walletSend(transaction.getFromWallet())
                    .walletRecive(transaction.getFromWallet())
                    .amount(transaction.getAmount())
                    .description(transaction.getDescription())
                    .time(transaction.getUpdatedAt())
                    .transactionId(transaction.getId())
                    .build();
            kafkaTemplate.send("wallet-transactions",transferMessage);
            return TransferResponse.builder()
                    .transactionId(transaction.getId())
                    .status(TransactionStatus.SUCCESS)
                    .amount(request.getAmount())
                    .fromWalletId(fromUserId)
                    .toWalletId(toUserId)
                    .createdAt(transaction.getCreatedAt())
                    .build();

        } catch (Exception e) {

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepnsitory.save(transaction);

            throw e;
        }
    }
}
