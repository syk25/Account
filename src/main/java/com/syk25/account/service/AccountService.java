package com.syk25.account.service;

import com.syk25.account.domain.Account;
import com.syk25.account.domain.AccountUser;
import com.syk25.account.dto.AccountDto;
import com.syk25.account.exception.AccountException;
import com.syk25.account.repository.AccountRepository;
import com.syk25.account.repository.AccountUserRepository;
import com.syk25.account.type.AccountStatus;
import com.syk25.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.syk25.account.type.AccountStatus.NOT_REGISTERED;
import static com.syk25.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는 지 조회
     * 계좌의 번호를 생성하고
     * 계좌를 저장하고, 그 정보를 넘긴다
     */

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {

        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        validateCreateAccount(accountUser);

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account ->
                        (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");


        return AccountDto.fromEntity(accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }


    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(NOT_REGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account); // 테스트 목적을 위한 코드

        return AccountDto.fromEntityForDelete(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ACCOUNT_AND_USER_MISMATCHED);
        }
        if (account.getAccountStatus() == NOT_REGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0) {
            throw new AccountException(BALANCE_EXISTS);
        }
    }


    @Transactional
    public Account getAccount(Long Id) {
        return accountRepository.getReferenceById(Id);
    }

    public List<AccountDto> getAccountsByUserId(Long userId) {

        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
