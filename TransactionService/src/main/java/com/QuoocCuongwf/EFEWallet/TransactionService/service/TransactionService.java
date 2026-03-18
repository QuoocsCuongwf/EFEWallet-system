package com.QuoocCuongwf.EFEWallet.TransactionService.service;

import com.QuoocCuongwf.EFEWallet.TransactionService.payload.request.TransferRequest;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.TransferResponse;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    public TransferResponse transfer (String userId, TransferRequest request, String idemKey){
        TransferResponse response;
        return response;
    }
}
