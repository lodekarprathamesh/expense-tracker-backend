package com.expensetracker.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String displayName
) {
}
