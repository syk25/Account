package com.syk25.account.controller;

import com.syk25.account.domain.Account;
import com.syk25.account.dto.AccountDto;
import com.syk25.account.dto.AccountInfo;
import com.syk25.account.dto.CreateAccount;
import com.syk25.account.dto.DeleteAccount;
import com.syk25.account.service.AccountService;
import com.syk25.account.service.RedisTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class AccountController {
    private final AccountService accountService;
    private final RedisTestService redisTestService;

    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }


    @PostMapping("/account")
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
    ) {
        return CreateAccount.Response.fromDto(accountService
                .createAccount(request.getUserId(),
                        request.getInitialBalance()));

    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount
            (@RequestBody @Valid DeleteAccount.Request request) {
        return DeleteAccount.Response.fromDto(accountService
                .deleteAccount(request.getUserId(),
                        request.getAccountNumber()));
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(@RequestParam("user_id") Long userId) {

        return accountService.getAccountsByUserId(userId)
                .stream().map(accountDto -> AccountInfo.builder()
                        .accountNumber((accountDto.getAccountNumber()))
                        .balance(accountDto.getBalance())
                        .build())
                .collect(Collectors.toList());

    }


    @GetMapping("/account/{Id}")
    public Account getAccount(@PathVariable("Id") Long Id) {
        return accountService.getAccount(Id);
    }
}
