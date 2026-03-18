package com.QuoocCuongwf.EFEWallet.wallet_system_api.enums;

public enum Role {
    USER,
    ADMIN;
    public String getAuthority() {
        return "ROLE_"+this.name();
    }
}
