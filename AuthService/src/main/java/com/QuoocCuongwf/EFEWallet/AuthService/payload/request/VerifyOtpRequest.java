package com.QuoocCuongwf.EFEWallet.AuthService.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class VerifyOtpRequest {
    private String identifier;
    private String action;
    private String otpCode;
}
