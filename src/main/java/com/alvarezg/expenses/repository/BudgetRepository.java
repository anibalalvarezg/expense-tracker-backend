package com.alvarezg.expenses.repository;

import com.alvarezg.expenses.model.Budget;
import com.alvarezg.expenses.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Buscar presupuesto general de un usuario por mes y año
    Optional<Budget> findByUserAndMonthAndYearAndCategoryIsNull(
            User user, int month, int year
    );
}