package com.QuoocsCuongwf.EFEWallet.WalletService.security;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OwnershipService {

    @Autowired
    private WalletRepository walletRepository;

    public boolean isOwner(Authentication authentication, UUID id) {
        String userId = authentication.getName();

        return walletRepository.findById(id)
                .map(wallet -> wallet.getUserId().toString().equals(userId))
                .orElse(false);
    }
}