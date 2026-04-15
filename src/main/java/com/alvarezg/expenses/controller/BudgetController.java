package com.alvarezg.expenses.controller;

import com.alvarezg.expenses.dto.BudgetRequest;
import com.alvarezg.expenses.dto.BudgetResponse;
import com.alvarezg.expenses.dto.BudgetStatusResponse;
import com.alvarezg.expenses.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Budgets", description = "Endpoints de presupuesto mensual")
@SecurityRequirement(name = "Bearer Auth")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Crear o actualizar presupuesto")
    @PostMapping
    public ResponseEntity<BudgetResponse> save(
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.save(request));
    }

    @Operation(summary = "Ver presupuesto del mes")
    @GetMapping
    public ResponseEntity<BudgetResponse> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getByMonth(year, month));
    }

    @Operation(summary = "Ver estado del presupuesto")
    @GetMapping("/status")
    public ResponseEntity<BudgetStatusResponse> getStatus(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getStatus(year, month));
    }
}