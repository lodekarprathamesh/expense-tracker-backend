package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> list(@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null || !currentUser.getUserId().equals(userId)) {
            throw new AccessDeniedException("Cannot access another user's data.");
        }
        return categoryRepository.findVisibleToUser(userId);
    }
}
