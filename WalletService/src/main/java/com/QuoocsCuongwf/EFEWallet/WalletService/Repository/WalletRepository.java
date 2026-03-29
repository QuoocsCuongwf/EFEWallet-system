package com.QuoocsCuongwf.EFEWallet.WalletService.Repository;

import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Boolean existsWalletEntitiesByUserId(UUID walletId,UUID userId);
    Optional<BigDecimal> getBalanceById(UUID walletId);
    Optional<WalletEntity> findById(UUID walletId);
    boolean existsWalletEntityByUserId(UUID userId);
}
