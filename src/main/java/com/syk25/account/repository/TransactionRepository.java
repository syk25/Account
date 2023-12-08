package com.syk25.account.repository;

import com.syk25.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

Optional<Transaction> findByTransactionId(String transactionId);

}
