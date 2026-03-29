package com.QuoocCuongwf.EFEWallet.TransactionService.aspect;

import com.QuoocCuongwf.EFEWallet.TransactionService.entity.IdempotencyKey;
import com.QuoocCuongwf.EFEWallet.TransactionService.enums.IdempotencyStatus;
import com.QuoocCuongwf.EFEWallet.TransactionService.exception.IdempotencyException;
import com.QuoocCuongwf.EFEWallet.TransactionService.reponsitory.IdempotencyKeyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.InvalidAlgorithmParameterException;
import java.util.Optional;

@Aspect
@Component
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IdempotentAspect {
    private IdempotencyKeyRepository idempotencyKeyRepository;
    private ObjectMapper objectMapper;
    @Around("@annotation(com.QuoocCuongwf.EFEWallet.TransactionService.annotation.Idempotent)")
    public Object HandlIdemponcy(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String key=request.getHeader("Idempotency-Key");
        if (key == null ||key.isBlank() ){
            throw new InvalidAlgorithmParameterException("Missing idemponcy key");
        }
        String payload = extractRequestBody(joinPoint.getArgs());
        String requestHash = DigestUtils.sha256Hex(payload);
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        String userId=auth.getName();
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByIdempotencyKeyAndUserId(key,userId);
        if (existing.isPresent()) {
            if (existing.get().getRequestHash().equals(requestHash)) {
                throw new IdempotencyException("Payload mismatch for existing idempotency key");
            }
            return ResponseEntity.ok().build();
        }

        IdempotencyKey record = IdempotencyKey.builder()
                .idempotencyKey(key)
                .userId(userId)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .build();

        idempotencyKeyRepository.save(record);

        try {
            Object result = joinPoint.proceed();

            record.setStatus(IdempotencyStatus.SUCCESS);
            record.setResponseBody(objectMapper.writeValueAsString(result));
            record.setResponseStatus(200);

            idempotencyKeyRepository.save(record);

            return result;

        } catch (Exception e) {
            record.setStatus(IdempotencyStatus.FAILED);
            idempotencyKeyRepository.save(record);
            throw e;
        }
    }

    private String extractRequestBody(Object[] args) {
        try {
            for (Object arg : args) {
                if (arg != null && !(arg instanceof ServletRequest) && !(arg instanceof ServletResponse)) {
                    return objectMapper.writeValueAsString(arg);
                }
            }
            return "";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body");
        }
    }

}
