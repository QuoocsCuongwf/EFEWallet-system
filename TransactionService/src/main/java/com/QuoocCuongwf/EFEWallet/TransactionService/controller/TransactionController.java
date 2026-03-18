package com.QuoocCuongwf.EFEWallet.TransactionService.controller;

import com.QuoocCuongwf.EFEWallet.TransactionService.config.SecurityConstants;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(SecurityConstants.API_ROOT+"/transaction")
public class TransactionController {
    @GetMapping("/me")
    public ApiResponse<Authentication> me(Authentication auth) {
        ApiResponse<Authentication> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage("ok");
        res.setData(auth);
        return res;
    }
}
