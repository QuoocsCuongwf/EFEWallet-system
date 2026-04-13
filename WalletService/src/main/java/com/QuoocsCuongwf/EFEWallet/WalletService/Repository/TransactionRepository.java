package com.QuoocsCuongwf.EFEWallet.WalletService.Repository;

import com.QuoocsCuongwf.EFEWallet.WalletService.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

}
