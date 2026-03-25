package com.QuoocCuongwf.EFEWallet.TransactionService.exception;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InsufficientBalanceException extends RuntimeException {
    private final int code = 400; // Bad Request

    public InsufficientBalanceException() {
        super("Balance not enough");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}