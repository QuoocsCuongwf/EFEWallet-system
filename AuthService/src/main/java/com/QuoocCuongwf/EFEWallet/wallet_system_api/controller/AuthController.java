package com.QuoocCuongwf.EFEWallet.wallet_system_api.controller;

import com.QuoocCuongwf.EFEWallet.wallet_system_api.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.response.ApiResponse;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SecurityConstants.AUTH_API)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = authService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login success")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request){
        RegisterResponse data = authService.register(request);
        ApiResponse<RegisterResponse> response = ApiResponse.<RegisterResponse>builder()
                .success(true)
                .message("Register success")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
