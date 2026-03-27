package com.QuoocsCuongwf.EFEWallet.WalletService.controller;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.WalletRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.annotation.IsOwner;
import com.QuoocsCuongwf.EFEWallet.WalletService.config.SecurityConstants;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.ApiResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.BalanceResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.WalletResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping(SecurityConstants.API_ROOT+SecurityConstants.WALLET_API)
public class WalletController {
    private final WalletService walletService;
    @GetMapping("/{id}/balance")
    @IsOwner
    public ResponseEntity<ApiResponse<BalanceResponse>> balance(
            @PathVariable UUID id,
            Authentication auth
    ) {
        log.info("Get balance for walletId={} by user={}", id, auth.getName());
        return ResponseEntity.ok(
                ApiResponse.success(walletService.balance(id))
        );
    }

    @GetMapping("/{id}")
    @IsOwner
    public ResponseEntity<ApiResponse<WalletResponse>> wallet(@PathVariable("id") UUID walletId){
        return ResponseEntity.ok(
                ApiResponse.success(walletService.wallet(walletId))
        );
    }
}
