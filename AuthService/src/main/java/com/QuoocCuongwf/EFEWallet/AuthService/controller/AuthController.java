package com.QuoocCuongwf.EFEWallet.AuthService.controller;

import com.QuoocCuongwf.EFEWallet.AuthService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.OtpRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.ApiResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.service.AuthService;
import com.QuoocCuongwf.EFEWallet.AuthService.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SecurityConstants.AUTH_API)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;
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
    @GetMapping("/logout")
    public ResponseEntity<?> logout(){
        authService.logout();
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<Void>> createOtp(@Valid @RequestBody OtpRequest otpRequest){
        boolean isSuccessful=otpService.generationOtp(otpRequest.getIdentifier(),otpRequest.getAction());
        if (isSuccessful) {
            ApiResponse<Void> successResponse = ApiResponse.<Void>builder()
                    .success(true)
                    .message("Yêu cầu thành công. Mã OTP đang được gửi tới địa chỉ liên lạc của bạn.")
                    .build();
            return ResponseEntity.ok(successResponse);
        } else {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Không thể tạo yêu cầu OTP lúc này. Vui lòng kiểm tra lại thông tin.")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

}
