package  com.alvarezg.expenses.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    @NotNull(message = "La categoría es requerida")
    private Long categoryId;

    private String description;

    @NotNull(message = "La fecha es requerida")
    private LocalDate date;
}