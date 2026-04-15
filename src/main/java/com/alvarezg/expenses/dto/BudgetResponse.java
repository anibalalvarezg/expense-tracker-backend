package com.alvarezg.expenses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private BigDecimal amount;
    private int month;
    private int year;
}