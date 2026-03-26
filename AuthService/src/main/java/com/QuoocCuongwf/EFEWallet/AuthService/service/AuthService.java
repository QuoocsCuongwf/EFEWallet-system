package com.QuoocCuongwf.EFEWallet.AuthService.service;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.Roles;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.RolesNotFoundException;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.reponsitory.RolesReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.reponsitory.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.security.CustomUserDetails;
import com.QuoocCuongwf.EFEWallet.AuthService.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserReponsitory userReponsitory;
    private final RolesReponsitory rolesReponsitory;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userReponsitory.findUserByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if(!user.isEnabled()) {
            throw  new DisabledException("User is disable");
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
        Roles role= rolesReponsitory.findRolesByName("USER")
                .orElseThrow(()->new RolesNotFoundException("USER"));
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .role(Set.of(role))
                .enabled(true)
                .build();
        User saved = userReponsitory.save(user);
        return new RegisterResponse(saved.getId(), saved.getEmail(), "Register success");
    }
}
