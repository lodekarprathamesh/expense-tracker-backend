package com.expensetracker.repository;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdOrderByTransactionTimeDesc(Long userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTransactionTimeBetweenOrderByTransactionTimeDesc(
            Long userId, Instant from, Instant to, Pageable pageable);

    Page<Transaction> findByUserIdAndCategoryIdOrderByTransactionTimeDesc(
            Long userId, Long categoryId, Pageable pageable);

    Optional<Transaction> findByUserIdAndSourceAndSourceReference(
            Long userId, TransactionSource source, String sourceReference);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionType = com.expensetracker.model.TransactionType.DEBIT
              AND t.transactionTime BETWEEN :from AND :to
            """)
    BigDecimal sumDebitsBetween(@Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionType = com.expensetracker.model.TransactionType.CREDIT
              AND t.transactionTime BETWEEN :from AND :to
            """)
    BigDecimal sumCreditsBetween(@Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);
}
