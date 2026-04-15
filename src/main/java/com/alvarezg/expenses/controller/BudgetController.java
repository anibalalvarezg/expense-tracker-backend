package com.alvarezg.expenses.controller;

import com.alvarezg.expenses.dto.BudgetRequest;
import com.alvarezg.expenses.dto.BudgetResponse;
import com.alvarezg.expenses.dto.BudgetStatusResponse;
import com.alvarezg.expenses.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    // POST /api/budgets
    @PostMapping
    public ResponseEntity<BudgetResponse> save(
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.save(request));
    }

    // GET /api/budgets?year=2026&month=4
    @GetMapping
    public ResponseEntity<BudgetResponse> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getByMonth(year, month));
    }

    // GET /api/budgets/status?year=2026&month=4
    @GetMapping("/status")
    public ResponseEntity<BudgetStatusResponse> getStatus(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getStatus(year, month));
    }
}