package com.alvarezg.expenses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetStatusResponse {
    private BigDecimal budget;        // presupuesto definido
    private BigDecimal spent;         // lo que ya gastaste
    private BigDecimal available;     // lo que te queda
    private double percentageUsed;    // % del presupuesto usado
    private boolean overBudget;       // ¿superaste el límite?
    private int month;
    private int year;
}