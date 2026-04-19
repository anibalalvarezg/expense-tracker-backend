package com.alvarezg.expenses.repository;

import  com.alvarezg.expenses.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Todos los gastos de un usuario ordenados por fecha
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    // Gastos de un usuario filtrados por mes y año
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    List<Expense> findByUserAndMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    // Total gastado por categoría en un mes
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND YEAR(e.date) = :year " +
            "AND MONTH(e.date) = :month GROUP BY e.category.name")
    List<Object[]> sumByCategory(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    // Gastos en un rango de fechas, ordenados por fecha DESC
    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Total gastado por categoría en un rango de fechas
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category.name")
    List<Object[]> sumByCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}