package com.alvarezg.expenses.service;

import com.alvarezg.expenses.dto.BudgetRequest;
import com.alvarezg.expenses.dto.BudgetResponse;
import com.alvarezg.expenses.dto.BudgetStatusResponse;
import com.alvarezg.expenses.exception.ResourceNotFoundException;
import com.alvarezg.expenses.model.Budget;
import com.alvarezg.expenses.model.User;
import com.alvarezg.expenses.repository.BudgetRepository;
import com.alvarezg.expenses.repository.ExpenseRepository;
import com.alvarezg.expenses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    // Crear o actualizar presupuesto del mes
    public BudgetResponse save(BudgetRequest request) {
        User user = getCurrentUser();

        // Si ya existe un presupuesto para ese mes lo actualiza
        Budget budget = budgetRepository
                .findByUserAndMonthAndYearAndCategoryIsNull(
                        user, request.getMonth(), request.getYear()
                )
                .orElse(Budget.builder()
                        .user(user)
                        .month(request.getMonth())
                        .year(request.getYear())
                        .build());

        budget.setAmount(request.getAmount());
        budgetRepository.save(budget);

        return new BudgetResponse(
                budget.getId(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear()
        );
    }

    // Ver presupuesto de un mes
    public BudgetResponse getByMonth(int year, int month) {
        User user = getCurrentUser();

        Budget budget = budgetRepository
                .findByUserAndMonthAndYearAndCategoryIsNull(user, month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay presupuesto definido para " + month + "/" + year
                ));

        return new BudgetResponse(
                budget.getId(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear()
        );
    }

    // Ver estado del presupuesto — cuánto gasté vs cuánto tengo
    public BudgetStatusResponse getStatus(int year, int month) {
        User user = getCurrentUser();

        Budget budget = budgetRepository
                .findByUserAndMonthAndYearAndCategoryIsNull(user, month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay presupuesto definido para " + month + "/" + year
                ));

        // Total gastado en el mes
        List<Object[]> summary = expenseRepository.sumByCategory(user.getId(), year, month);
        BigDecimal spent = summary.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = budget.getAmount().subtract(spent);
        boolean overBudget = spent.compareTo(budget.getAmount()) > 0;

        double percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) == 0
                ? 0
                : spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100))
                  .doubleValue();

        return new BudgetStatusResponse(
                budget.getAmount(),
                spent,
                available,
                percentageUsed,
                overBudget,
                month,
                year
        );
    }
}