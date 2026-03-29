package com.QuoocCuongwf.EFEWallet.TransactionService.exception;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class WalletNotFoundException extends RuntimeException {

    private final int code = 4040;

    public WalletNotFoundException() {
        super("Wallet not found");
    }

    public WalletNotFoundException(String message) {
        super(message);
    }
}