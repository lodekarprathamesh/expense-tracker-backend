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
import lombok.extern.slf4j.Slf4j;

/**
 * userId is still part of the URL (keeps the API RESTful and readable), but every
 * endpoint now verifies it matches the authenticated principal from the JWT.
 * A user can only ever read/write their own data.
 */
@Slf4j
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
    public ResponseEntity<?> createFromAutoCapture(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AutoCaptureTransactionRequest request) {

        log.info("=== STARTING AUTO-CAPTURE REQUEST ===");
        log.info("Target UserId: {}", userId);
        log.info("Amount: {}", request.amount());
        log.info("TransactionType: {}", request.transactionType());
        log.info("Merchant: {}", request.merchant());
        log.info("Source: {}", request.source());
        log.info("SourceReference: {}", request.sourceReference());
        log.info("TransactionTime: {}", request.transactionTime());

        try {
            log.info("Step 1: Checking requireSelf authorization...");
            requireSelf(userId, currentUser);

            log.info("Step 2: Sending data to TransactionService...");
            TransactionResponse response = transactionService.createFromAutoCapture(userId, request);

            log.info("Step 3: Success! Saved transaction with ID: {}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("=== 🚨 CRASH DETECTED IN AUTO-CAPTURE 🚨 ===");
            log.error("Exception Class: {}", e.getClass().getName());
            log.error("Error Message: {}", e.getMessage());
            e.printStackTrace();

            String errorMessage = e.getMessage() != null ? e.getMessage() : "Null Error Message";
            String cause = (e.getCause() != null) ? e.getCause().toString() : "No inner cause";

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                            "error", "CRASH: " + errorMessage,
                            "cause", cause,
                            "class", e.getClass().getName()
                    ));
        }
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
