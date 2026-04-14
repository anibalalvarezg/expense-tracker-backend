package com.alvarezg.expenses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private BigDecimal totalMonth;
    private Map<String, BigDecimal> byCategory;
}