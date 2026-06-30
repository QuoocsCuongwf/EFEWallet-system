package com.QuoocsCuongwf.EFEEWallet.NotificationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OtpMessage {
    private String identifier;
    private String otp;
    private String action;

}
