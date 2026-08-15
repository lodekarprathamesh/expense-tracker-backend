package com.expensetracker.controller;

import com.expensetracker.dto.CreateAccountRequest;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Account;
import com.expensetracker.model.User;
import com.expensetracker.repository.AccountRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Account> create(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateAccountRequest request) {
        requireSelf(userId, currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Account account = Account.builder()
                .user(user)
                .bankName(request.bankName())
                .nickname(request.nickname())
                .accountSuffix(request.accountSuffix())
                .accountType(request.accountType())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(accountRepository.save(account));
    }

    @GetMapping
    public List<Account> list(@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        requireSelf(userId, currentUser);
        return accountRepository.findByUserId(userId);
    }

    private void requireSelf(Long pathUserId, CustomUserDetails currentUser) {
        if (currentUser == null || !currentUser.getUserId().equals(pathUserId)) {
            throw new AccessDeniedException("Cannot access another user's data.");
        }
    }
}
