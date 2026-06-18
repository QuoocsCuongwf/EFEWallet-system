package com.QuoocCuongwf.EFEWallet.AuthService.service;

import com.QuoocCuongwf.EFEWallet.AuthService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.Roles;
import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.enums.WalletStatus;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.EmailNotExistException;
import com.QuoocCuongwf.EFEWallet.AuthService.exception.RolesNotFoundException;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.dto.WalletDto;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.LoginRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.request.RegisterRequest;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.LoginResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.payload.response.RegisterResponse;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.RolesReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.security.CustomUserDetails;
import com.QuoocCuongwf.EFEWallet.AuthService.security.JwtService;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.WalletServiceGrpc;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.GenReq;
import com.QuoocsCuongwf.EFEWallet.WalletService.grpc.GenRes;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import tools.jackson.core.io.BigDecimalParser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserReponsitory userReponsitory;
    private final RolesReponsitory rolesReponsitory;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final RedisTemplate<Object,Object> redisTemplate;

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
        User user = userReponsitory.findUserByEmail(request.getEmail()).orElseThrow(()->new EmailNotExistException(request.getEmail()));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if(!user.isEnabled()) {
            throw new DisabledException("User is disable");
        }
        CustomUserDetails details = new CustomUserDetails(user);
        String token = jwtService.generateToken(details);
        String refeshToken = jwtService.generateToken(details);
        return new LoginResponse(token, refeshToken, jwtService.getExpirationMs());
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        boolean existing = userReponsitory.existsByEmail(request.getEmail());
        if (existing) {
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
                .enabled(false)
                .build();
        String key = "REGISTER:PENDING:" + request.getEmail();


        redisTemplate.opsForValue().set(key,user,5, TimeUnit.MINUTES);

        return new RegisterResponse<>(
                null, // ID là null vì chưa lưu xuống Database
                user.getEmail(),
                "Đăng ký bước 1 thành công. Vui lòng kiểm tra Email để lấy mã OTP.",
                null
        );
    }
    @Transactional
    public void verifyAndSaveRegisterUser(String identifier, String otp){
        otpService.verifyOtp(identifier, SecurityConstants.ACTION_REG,otp);

        String redisKey = "REGISTER:PENDING:" + identifier;
        User user = (User) redisTemplate.opsForValue().get(redisKey);

        if (user == null) {
            throw new RuntimeException("Register session expired!");
        }
        user.setEnabled(true);
        userReponsitory.save(user);

        redisTemplate.delete(redisKey);
    }

    public void logout(){
       String token=jwtService.getAccessToken();
        jwtService.addBlackListToken(token);
    }
}
