package com.syk25.account.service;

import com.syk25.account.domain.Account;
import com.syk25.account.domain.AccountUser;
import com.syk25.account.domain.Transaction;
import com.syk25.account.dto.TransactionDto;
import com.syk25.account.exception.AccountException;
import com.syk25.account.repository.AccountRepository;
import com.syk25.account.repository.AccountUserRepository;
import com.syk25.account.repository.TransactionRepository;
import com.syk25.account.type.AccountStatus;
import com.syk25.account.type.ErrorCode;
import com.syk25.account.type.TransactionResultType;
import com.syk25.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.syk25.account.type.ErrorCode.*;
import static com.syk25.account.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.syk25.account.type.ErrorCode.TRANSACTION_CANCEL_AVAILABLE_DATE_EXPIRED;
import static com.syk25.account.type.TransactionResultType.F;
import static com.syk25.account.type.TransactionResultType.S;
import static com.syk25.account.type.TransactionType.CANCEL;
import static com.syk25.account.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {


        AccountUser user = accountUserRepository
                .findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(USE, S, amount, account));
    }


    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {

            throw new AccountException(ACCOUNT_AND_USER_MISMATCHED);
        }
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() < amount) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException((ACCOUNT_NOT_FOUND)));

        saveAndGetTransaction(USE, F, amount, account);
    }

    private Transaction saveAndGetTransaction(TransactionType transactionType, TransactionResultType transactionResultType, Long amount, Account account) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapShot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build());
    }

    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
        validateCancelBalance(transaction, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(CANCEL, S, amount, account));
    }

    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(TRANSACTION_UNMATCHED);
        }
        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(FULL_CANCEL_REQUIRED);
        }
        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1L))) {
            throw new AccountException(TRANSACTION_CANCEL_AVAILABLE_DATE_EXPIRED);
        }
        ;
    }

    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException((ACCOUNT_NOT_FOUND)));

        saveAndGetTransaction(CANCEL, F, amount, account);
    }
}
