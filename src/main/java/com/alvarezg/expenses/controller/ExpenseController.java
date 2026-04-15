package com.alvarezg.expenses.controller;

import com.alvarezg.expenses.dto.ExpenseRequest;
import com.alvarezg.expenses.dto.ExpenseResponse;
import com.alvarezg.expenses.dto.ExpenseSummaryResponse;
import com.alvarezg.expenses.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Expenses", description = "Endpoints de gastos")
@SecurityRequirement(name = "Bearer Auth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Listar todos los gastos")
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAll() {
        return ResponseEntity.ok(expenseService.getAll());
    }

    @Operation(summary = "Listar gastos por mes")
    @GetMapping("/month")
    public ResponseEntity<List<ExpenseResponse>> getByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getByMonth(year, month));
    }

    @Operation(summary = "Resumen mensual por categoría")
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> getSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(expenseService.getSummary(year, month));
    }

    @Operation(summary = "Crear gasto")
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.create(request));
    }

    @Operation(summary = "Actualizar gasto")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    @Operation(summary = "Eliminar gasto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}