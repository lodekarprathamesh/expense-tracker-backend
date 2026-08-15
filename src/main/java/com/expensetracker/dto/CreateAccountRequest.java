package com.expensetracker.dto;

import com.expensetracker.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotBlank String bankName,
        String nickname,
        String accountSuffix,
        @NotNull AccountType accountType
) {
}
