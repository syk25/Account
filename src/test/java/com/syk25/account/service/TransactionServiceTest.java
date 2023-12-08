package com.syk25.account.service;

import com.syk25.account.domain.Account;
import com.syk25.account.domain.AccountUser;
import com.syk25.account.domain.Transaction;
import com.syk25.account.dto.TransactionDto;
import com.syk25.account.repository.AccountRepository;
import com.syk25.account.repository.AccountUserRepository;
import com.syk25.account.repository.TransactionRepository;
import com.syk25.account.type.AccountStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.syk25.account.type.TransactionResultType.S;
import static com.syk25.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void useBalance_success() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
        given(transactionRepository.save(any())).willReturn(Transaction.builder()
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .account(account)
                .amount(1000L)
                .balanceSnapShot(9000L)
                .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 200L);
        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L,captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapShot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(1000L, transactionDto.getAmount());
    }
}