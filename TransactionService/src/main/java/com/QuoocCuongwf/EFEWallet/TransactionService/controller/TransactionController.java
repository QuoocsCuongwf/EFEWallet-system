package com.QuoocCuongwf.EFEWallet.TransactionService.controller;

import com.QuoocCuongwf.EFEWallet.TransactionService.annotation.Idempotent;
import com.QuoocCuongwf.EFEWallet.TransactionService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.TransactionService.enums.TransactionType;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.request.TransferRequest;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.ApiResponse;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.TransferResponse;
import com.QuoocCuongwf.EFEWallet.TransactionService.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SecurityConstants.API_ROOT+"/transaction")
public class TransactionController {
    private TransactionService transactionService;
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Authentication>> me(Authentication auth) {
        ApiResponse<Authentication> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage("ok");
        res.setData(auth);
        return ResponseEntity.ok(res);
    }
    @PostMapping("/transfer")
    @Idempotent(
            type = TransactionType.TRANSFER,
            validatePayload = true
    )
    public ResponseEntity<ApiResponse<TransferResponse>> transfer (
            @RequestHeader("idempotency-Key") String idemKey,
            Authentication auth,
            @RequestBody TransferRequest transferRequest){
        String userId=auth.getName();
        TransferResponse transferResponse = transactionService.transfer(transferRequest);
        ApiResponse<TransferResponse> res = new ApiResponse<>(true,"Transfer susscessfully",transferResponse);
        return ResponseEntity.ok(res);

    }
}
