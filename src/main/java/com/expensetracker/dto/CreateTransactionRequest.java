package com.expensetracker.dto;

import com.expensetracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateTransactionRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull TransactionType transactionType,
        String merchant,
        String note,
        Long accountId,
        Long categoryId,
        @NotNull Instant transactionTime
) {
}
