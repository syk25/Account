package com.syk25.account.dto;

import com.syk25.account.type.TransactionResultType;
import com.syk25.account.type.TransactionType;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTransactionResponse {

    private String accountNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResultType;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactedAt;

    public static QueryTransactionResponse from(TransactionDto transactionDto){
        return QueryTransactionResponse.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .transactionResultType(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTransactionId())
                .amount(transactionDto.getAmount())
                .transactedAt(transactionDto.getTransactedAt())
                .build();
    }
}
