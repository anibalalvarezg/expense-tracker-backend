package com.alvarezg.expenses.service;

import com.alvarezg.expenses.dto.ExpenseRequest;
import com.alvarezg.expenses.dto.ExpenseResponse;
import com.alvarezg.expenses.dto.ExpenseSummaryResponse;
import com.alvarezg.expenses.model.Category;
import com.alvarezg.expenses.model.Expense;
import com.alvarezg.expenses.model.User;
import com.alvarezg.expenses.repository.CategoryRepository;
import com.alvarezg.expenses.repository.ExpenseRepository;
import com.alvarezg.expenses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Obtener usuario autenticado
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Mapear Expense → ExpenseResponse
    private ExpenseResponse toResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setDate(expense.getDate());
        if (expense.getCategory() != null) {
            response.setCategoryName(expense.getCategory().getName());
            response.setCategoryIcon(expense.getCategory().getIcon());
            response.setCategoryColor(expense.getCategory().getColor());
        }
        return response;
    }

    // Listar todos los gastos del usuario
    public List<ExpenseResponse> getAll() {
        User user = getCurrentUser();
        return expenseRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Listar gastos por mes
    public List<ExpenseResponse> getByMonth(int year, int month) {
        User user = getCurrentUser();
        return expenseRepository.findByUserAndMonth(user.getId(), year, month)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Crear gasto
    public ExpenseResponse create(ExpenseRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Expense expense = Expense.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    // Actualizar gasto
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        // Verificar que el gasto pertenece al usuario
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para editar este gasto");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setCategory(category);

        return toResponse(expenseRepository.save(expense));
    }

    // Eliminar gasto
    public void delete(Long id) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este gasto");
        }

        expenseRepository.delete(expense);
    }

    // Resumen mensual
    public ExpenseSummaryResponse getSummary(int year, int month) {
        User user = getCurrentUser();

        List<Object[]> results = expenseRepository.sumByCategory(user.getId(), year, month);

        Map<String, BigDecimal> byCategory = results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        BigDecimal total = byCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExpenseSummaryResponse(total, byCategory);
    }
}