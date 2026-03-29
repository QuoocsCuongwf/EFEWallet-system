package com.QuoocCuongwf.EFEWallet.TransactionService.reponsitory;

import com.QuoocCuongwf.EFEWallet.TransactionService.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionReponsitory extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findTransactionByWalletId(UUID userId);

}
