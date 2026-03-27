package com.QuoocsCuongwf.EFEWallet.WalletService.service;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.WalletRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.WalletNotFoundException;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.BalanceResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.WalletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    public BalanceResponse balance (UUID walletId){
        BigDecimal balance = walletRepository.getBalanceByWalletId(walletId)
                .orElseThrow(()-> new WalletNotFoundException("Wallet " + walletId + " not found"));
        BalanceResponse balanceResponse=BalanceResponse.builder()
                .balance(balance)
                .time(LocalDateTime.now())
                .build();
        return balanceResponse;
    }

    public WalletResponse wallet (UUID walletId){
        WalletEntity walletEntity=walletRepository.getWalletEntitiesByWalletId(walletId)
                .orElseThrow(()-> new WalletNotFoundException("Wallet " + walletId + " not found"));
        return WalletResponse.builder()
                .walletId(walletEntity.getId())
                .walletAddress(walletEntity.getWalletAddress())
                .userId(walletEntity.getUserId())
                .status(walletEntity.getWalletStatus())
                .createAt(walletEntity.getCreatedAt())
                .build();
    }
}
