package com.expensetracker.dto;

import java.math.BigDecimal;

public record SummaryResponse(BigDecimal totalDebits, BigDecimal totalCredits, BigDecimal net) {
}
