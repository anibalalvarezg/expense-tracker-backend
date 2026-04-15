package com.alvarezg.expenses.repository;

import com.alvarezg.expenses.model.Budget;
import com.alvarezg.expenses.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserAndMonthAndYearAndCategoryIsNull(
            User user, int month, int year
    );

    // Todos los budgets generales de un mes específico
    @Query("SELECT b FROM Budget b WHERE b.month = :month " +
            "AND b.year = :year AND b.category IS NULL")
    List<Budget> findAllByMonthAndYear(
            @Param("month") int month,
            @Param("year") int year
    );

    // Verificar si ya existe budget para ese usuario/mes/año
    boolean existsByUserAndMonthAndYearAndCategoryIsNull(
            User user, int month, int year
    );
}