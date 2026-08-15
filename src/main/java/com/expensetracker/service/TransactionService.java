package com.expensetracker.service;

import com.expensetracker.dto.AutoCaptureTransactionRequest;
import com.expensetracker.dto.CreateTransactionRequest;
import com.expensetracker.dto.SummaryResponse;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.exception.DuplicateTransactionException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionSource;
import com.expensetracker.model.User;
import com.expensetracker.repository.AccountRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponse createManual(Long userId, CreateTransactionRequest request) {
        User user = getUserOrThrow(userId);

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.amount())
                .transactionType(request.transactionType())
                .merchant(request.merchant())
                .note(request.note())
                .source(TransactionSource.MANUAL)
                .transactionTime(request.transactionTime())
                .build();

        if (request.accountId() != null) {
            transaction.setAccount(accountRepository.findById(request.accountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountId())));
        }
        if (request.categoryId() != null) {
            transaction.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId())));
        }

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse createFromAutoCapture(Long userId, AutoCaptureTransactionRequest request) {
        User user = getUserOrThrow(userId);

        if (request.sourceReference() != null) {
            transactionRepository.findByUserIdAndSourceAndSourceReference(
                    userId, request.source(), request.sourceReference()
            ).ifPresent(existing -> {
                throw new DuplicateTransactionException(
                        "Transaction already recorded for reference: " + request.sourceReference());
            });
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.amount())
                .transactionType(request.transactionType())
                .merchant(request.merchant())
                .source(request.source())
                .sourceReference(request.sourceReference())
                .rawCaptureText(request.rawCaptureText())
                .transactionTime(request.transactionTime())
                .build();

        if (request.accountSuffix() != null) {
            accountRepository.findByUserId(userId).stream()
                    .filter(a -> request.accountSuffix().equals(a.getAccountSuffix()))
                    .findFirst()
                    .ifPresent(transaction::setAccount);
        }

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    public Page<TransactionResponse> list(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByTransactionTimeDesc(userId, pageable)
                .map(TransactionResponse::from);
    }

    public SummaryResponse summary(Long userId, Instant from, Instant to) {
        var totalDebits = transactionRepository.sumDebitsBetween(userId, from, to);
        var totalCredits = transactionRepository.sumCreditsBetween(userId, from, to);
        return new SummaryResponse(totalDebits, totalCredits, totalCredits.subtract(totalDebits));
    }

    public TransactionResponse get(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transaction not found: " + transactionId);
        }
        transactionRepository.delete(transaction);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
