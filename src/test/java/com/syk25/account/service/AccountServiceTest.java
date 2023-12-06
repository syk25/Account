package com.syk25.account.service;

import com.syk25.account.domain.Account;
import com.syk25.account.domain.AccountUser;
import com.syk25.account.dto.AccountDto;
import com.syk25.account.exception.AccountException;
import com.syk25.account.repository.AccountUserRepository;
import com.syk25.account.type.AccountStatus;
import com.syk25.account.repository.AccountRepository;
import com.syk25.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void createAccount_success() {
        // given
        AccountUser user = AccountUser.builder().id(12L)
                .name("Pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("100000000").build()));

        given(accountRepository.save(any()))
                .willReturn((Account.builder()
                        .accountUser(user)
                        .accountNumber("100000013").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService
                .createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("100000013", accountDto.getAccountNumber());

    }

    @Test
    public void createFirstAccount_success() {
        // given
        AccountUser user = AccountUser.builder().id(15L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn((Account.builder()
                        .accountUser(user)
                        .accountNumber("100000015").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService
                .createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("100000015", accountDto.getAccountNumber());

    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    public void createAccount_UserNotFound() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    public void deleteAccount_success() {
        // given
        AccountUser user = AccountUser.builder().id(12L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));

        // when
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        AccountDto accountDto = accountService
                .deleteAccount(1L, "1234567890");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.NOT_REGISTERED, captor.getValue().getAccountStatus());

    }

    @Test
    @DisplayName("해당 사용자 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException exception = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    public void deleteAccount_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder().id(12L)
                .name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());


        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService
                        .deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름")
    public void deleteAccount_userUnMatch() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        AccountUser harry = AccountUser.builder()
                .id(13L)
                .name("Harry")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService
                        .deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.ACCOUNT_AND_USER_MISMATCHED, exception.getErrorCode());

    }

    @Test
    @DisplayName("해당 계좌 잔액 존재")
    public void deleteAccount_balanceNotEmpty() {
        // given
        AccountUser pobi = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> accountService
                        .deleteAccount(1L, "1234567890"));

        // then
        assertEquals(ErrorCode.BALANCE_EXISTS, exception.getErrorCode());
    }

}
