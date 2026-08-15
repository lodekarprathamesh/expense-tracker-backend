package com.expensetracker.dto;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionSource;
import com.expensetracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType transactionType,
        String merchant,
        String note,
        Long accountId,
        String accountNickname,
        Long categoryId,
        String categoryName,
        TransactionSource source,
        String sourceReference,
        Instant transactionTime,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getTransactionType(),
                t.getMerchant(),
                t.getNote(),
                t.getAccount() != null ? t.getAccount().getId() : null,
                t.getAccount() != null ? t.getAccount().getNickname() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getSource(),
                t.getSourceReference(),
                t.getTransactionTime(),
                t.getCreatedAt()
        );
    }
}
