package com.expensetracker.controller;

import com.expensetracker.dto.AutoCaptureTransactionRequest;
import com.expensetracker.dto.CreateTransactionRequest;
import com.expensetracker.dto.SummaryResponse;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.security.CustomUserDetails;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * userId is still part of the URL (keeps the API RESTful and readable), but every
 * endpoint now verifies it matches the authenticated principal from the JWT.
 * A user can only ever read/write their own data.
 */
@RestController
@RequestMapping("/api/users/{userId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createManual(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateTransactionRequest request) {
        requireSelf(userId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createManual(userId, request));
    }

    @PostMapping("/auto-capture")
    public ResponseEntity<TransactionResponse> createFromAutoCapture(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AutoCaptureTransactionRequest request) {
        requireSelf(userId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createFromAutoCapture(userId, request));
    }

    @GetMapping
    public Page<TransactionResponse> list(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        requireSelf(userId, currentUser);
        return transactionService.list(userId, pageable);
    }

    @GetMapping("/summary")
    public SummaryResponse summary(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        requireSelf(userId, currentUser);
        return transactionService.summary(userId, from, to);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse get(
            @PathVariable Long userId,
            @PathVariable Long transactionId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        requireSelf(userId, currentUser);
        return transactionService.get(userId, transactionId);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long transactionId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        requireSelf(userId, currentUser);
        transactionService.delete(userId, transactionId);
        return ResponseEntity.noContent().build();
    }

    private void requireSelf(Long pathUserId, CustomUserDetails currentUser) {
        if (currentUser == null || !currentUser.getUserId().equals(pathUserId)) {
            throw new AccessDeniedException("Cannot access another user's data.");
        }
    }
}
