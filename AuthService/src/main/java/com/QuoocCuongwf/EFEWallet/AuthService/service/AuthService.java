package com.QuoocCuongwf.EFEWallet.AuthService.service;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.Roles;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.enums.WalletStatus;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.RolesNotFoundException;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.dto.WalletDto;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.reponsitory.RolesReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.reponsitory.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.security.CustomUserDetails;
import com.QuoocCuongwf.EFEWallet.AuthService.security.JwtService;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.WalletServiceGrpc;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.GenReq;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.GenRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import tools.jackson.core.io.BigDecimalParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserReponsitory userReponsitory;
    private final RolesReponsitory rolesReponsitory;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${spring.grpc.client.wallet-service.address:static://localhost:9090}")
    private String walletServiceAddress;

    @Value("${spring.grpc.client.wallet-service.negotiation-type:plaintext}")
    private String negotiationType;

    private ManagedChannel walletChannel;
    private WalletServiceGrpc.WalletServiceBlockingStub walletStub;

    @PostConstruct
    void initWalletStub() {
        String endpoint = walletServiceAddress.replace("static://", "");
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(endpoint);
        if ("plaintext".equalsIgnoreCase(negotiationType)) {
            builder.usePlaintext();
        }
        walletChannel = builder.build();
        walletStub = WalletServiceGrpc.newBlockingStub(walletChannel);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userReponsitory.findUserByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if(!user.isEnabled()) {
            throw new DisabledException("User is disable");
        }
        CustomUserDetails details = new CustomUserDetails(user);
        String token = jwtService.generateToken(details);
        return new LoginResponse(token, null, jwtService.getExpirationMs());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        User existing = userReponsitory.findUserByEmail(request.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        Roles role = rolesReponsitory.findRolesByName("USER")
                .orElseThrow(() -> new RolesNotFoundException("USER"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Set.of(role))
                .enabled(true)
                .build();

        User saved = userReponsitory.save(user);
        WalletDto walletDto;
        try {
            GenReq walletRequest = GenReq.newBuilder()
                    .setUserId(saved.getId().toString())
                    .build();

            GenRes walletResponse = walletStub.generation(walletRequest);
            walletDto = WalletDto.builder()
                    .walletAddress(walletResponse.getWalletAddress())
                    .status(WalletStatus.ACTIVATE)
                    .balance(BigDecimalParser.parse(walletResponse.getBalance()))
                    .createAt(LocalDateTime.now()) // Sửa lỗi chính tả .now()
                    .build();
            System.out.println("Wallet created: " + walletResponse.getWalletAddress());
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo ví, đăng ký thất bại: " + e.getMessage());
        }
        return new RegisterResponse<>(
                saved.getId(),
                saved.getEmail(),
                "Register success",
                walletDto
        );
    }
}
