package com.QuoocCuongwf.EFEWallet.TransactionService.service;

import com.QuoocCuongwf.EFEWallet.TransactionService.payload.request.TransferRequest;
import com.QuoocCuongwf.EFEWallet.TransactionService.payload.response.TransferResponse;
import com.QuoocCuongwf.EFEWallet.TransactionService.reponsitory.TransactionReponsitory;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {
    private TransactionReponsitory transactionReponsitory;
    @Transactional
    public TransferResponse transfer(TransferRequest transferRequest){
        UUID walletId=transferRequest.getToWalletId();
        TransferResponse transferResponse = TransferResponse.builder().build();
        return transferResponse;
    }
}
