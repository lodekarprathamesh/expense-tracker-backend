package com.expensetracker.dto;

import com.expensetracker.model.TransactionSource;
import com.expensetracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload sent by the Android app's parsing pipeline (Phase 4: NotificationListenerService)
 * after it has extracted structured fields from a bank notification.
 */
public record AutoCaptureTransactionRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull TransactionType transactionType,
        String merchant,
        String accountSuffix,
        @NotNull TransactionSource source,
        String sourceReference,
        @NotBlank String rawCaptureText,
        @NotNull Instant transactionTime
) {
}
