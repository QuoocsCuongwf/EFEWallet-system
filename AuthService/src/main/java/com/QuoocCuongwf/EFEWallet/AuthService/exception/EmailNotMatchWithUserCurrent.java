package com.QuoocCuongwf.EFEWallet.AuthService.exception;

public class EmailNotMatchWithUserCurrent extends RuntimeException {
    public EmailNotMatchWithUserCurrent(String message) {
        super(message);
    }
}
