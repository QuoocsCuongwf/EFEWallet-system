package com.QuoocsCuongwf.EFEWallet.WalletService.controller;

import com.QuoocsCuongwf.EFEWallet.WalletService.annotation.IsOwner;
import com.QuoocsCuongwf.EFEWallet.WalletService.config.SecurityConstants;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.ApiResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.BalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping(SecurityConstants.API_ROOT+SecurityConstants.WALLET_API)
public class WalletController {
    @GetMapping("/{id}")
    @IsOwner(walletId = "id")
    public ResponseEntity<ApiResponse<BalanceResponse>> balance(
            @PathVariable UUID id,
            Authentication auth
    ) {

        String username = auth.getName(); // hoặc cast Principal
        BalanceResponse balance = walletService.getBalance(id);
        return ResponseEntity.ok(
                ApiResponse.success(balance)
        );
    }
}
