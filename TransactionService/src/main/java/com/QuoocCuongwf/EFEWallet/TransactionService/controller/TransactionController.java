package com.QuoocCuongwf.EFEWallet.TransactionService.controller;

import com.QuoocCuongwf.EFEWallet.TransactionService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.request.TransferRequest;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.ApiResponse;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.TransferResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SecurityConstants.API_ROOT+"/transaction")
public class TransactionController {
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Authentication>> me(Authentication auth) {
        ApiResponse<Authentication> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage("ok");
        res.setData(auth);
        return ResponseEntity.ok(res);
    }
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer (
            @RequestHeader("Idempotency-Key") String idemKey,
            Authentication auth,
            @RequestBody TransferRequest transferRequest){
        String userId=auth.getName();
        return ResponseEntity.ok("ok");
    }
}
