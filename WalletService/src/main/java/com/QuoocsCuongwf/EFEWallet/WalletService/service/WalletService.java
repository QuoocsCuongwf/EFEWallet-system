package com.QuoocsCuongwf.EFEWallet.WalletService.service;

import com.QuoocsCuongwf.EFEWallet.WalletService.Enum.WalletStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.WalletRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.WalletIsExistedException;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.WalletNotFoundException;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.BalanceResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.WalletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@AllArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public BalanceResponse balance (UUID walletId){
        BigDecimal balance = walletRepository.getBalanceById(walletId)
                .orElseThrow(()-> new WalletNotFoundException("Wallet " + walletId + " not found"));
        BalanceResponse balanceResponse=BalanceResponse.builder()
                .balance(balance)
                .time(LocalDateTime.now())
                .build();
        return balanceResponse;
    }

    public WalletResponse wallet (UUID walletId){
        WalletEntity walletEntity=walletRepository.findById(walletId)
                .orElseThrow(()-> new WalletNotFoundException("Wallet " + walletId + " not found"));
        return WalletResponse.builder()
                .walletId(walletEntity.getId())
                .walletAddress(walletEntity.getWalletAddress())
                .userId(walletEntity.getUserId())
                .status(walletEntity.getWalletStatus())
                .createAt(walletEntity.getCreatedAt())
                .build();
    }

    public WalletResponse generation (UUID userId) {
        if (walletRepository.existsWalletEntityByUserId(userId)) {
            throw new WalletIsExistedException(userId.toString() + " had wallet");
        }
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);

        StringBuilder sb = new StringBuilder("0x");
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        String address = sb.toString();

        WalletEntity wallet = WalletEntity.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .walletAddress(address)
                .walletStatus(WalletStatus.ACTIVATE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        walletRepository.save(wallet);

        return WalletResponse.builder()
                .walletId(wallet.getId())
                .walletAddress(wallet.getWalletAddress())
                .status(wallet.getWalletStatus())
                .build();
    }

    public void generationOtp(UUID userId,String action) {
        SecureRandom random = new SecureRandom();
        String otp = String.valueOf(100000 + random.nextInt(900000));
        String hash = encoder.encode(otp);
        String key = "otp:" + action + ":" + userId;
        Map<Object,Object> value=new HashMap<>();
        value.put("otpHash", hash);
        value.put("attempt", 0);
        value.put("maxAttempt", 5);

        redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
    }
    public void verifyOtp(String userId, String action, String inputOtp) {

        String key = "otp:" + action + ":" + userId;
        Map<String, Object> data = (Map<String, Object>) redisTemplate.opsForValue().get(key);

        if (data == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        int attempt = (int) data.get("attempt");
        int maxAttempt = (int) data.get("maxAttempt");

        if (attempt >= maxAttempt) {
            redisTemplate.delete(key);
            throw new RuntimeException("Too many attempts");
        }

        String hash = (String) data.get("otpHash");

        if (!encoder.matches(inputOtp, hash)) {
            data.put("attempt", attempt + 1);
            redisTemplate.opsForValue().set(key, data);
            throw new RuntimeException("Invalid OTP");
        }
        redisTemplate.delete(key);
    }
}
