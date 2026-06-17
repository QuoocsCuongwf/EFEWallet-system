package com.QuoocCuongwf.EFEWallet.AuthService.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OtpRequest {
    private String identifier;
    private String action;
}
