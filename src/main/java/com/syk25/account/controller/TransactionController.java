package com.syk25.account.controller;


import com.syk25.account.dto.CancelBalance;
import com.syk25.account.dto.QueryTransactionResponse;
import com.syk25.account.dto.UseBalance;
import com.syk25.account.exception.AccountException;
import com.syk25.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 거래 확인
 */

@RestController
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request
    ) {
        try {
            return UseBalance.Response.from(
                    transactionService.useBalance(request.getUserId(), request.getAccountNumber(),
                            request.getAmount()));
        } catch (AccountException e) {
            log.error("잔액 사용이 불가합니다.");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ) {
        try {
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(request.getTransactionId(), request.getAccountNumber(),
                            request.getAmount()));
        } catch (AccountException e) {
            log.error("잔액 사용이 불가합니다.");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }
    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(@PathVariable("transactionId") String transactionId){
        return QueryTransactionResponse.from(transactionService.queryTransaction(transactionId));

    }
}
