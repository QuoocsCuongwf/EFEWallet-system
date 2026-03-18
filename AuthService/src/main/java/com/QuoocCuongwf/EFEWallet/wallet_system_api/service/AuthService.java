package com.QuoocCuongwf.EFEWallet.wallet_system_api.service;

import com.QuoocCuongwf.EFEWallet.wallet_system_api.entity.User;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.repository.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.security.CustomUserDetails;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.enums.Role;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserReponsitory userReponsitory;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userReponsitory.findUserByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        CustomUserDetails details = new CustomUserDetails(user);
        String token = jwtService.generateToken(details);
        return new LoginResponse(token, null, jwtService.getExpirationMs());
    }

    public RegisterResponse register(RegisterRequest request) {
        User existing = userReponsitory.findUserByEmail(request.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .enabled(true)
                .build();
        User saved = userReponsitory.save(user);
        return new RegisterResponse(saved.getId(), saved.getEmail(), "Register success");
    }
}
