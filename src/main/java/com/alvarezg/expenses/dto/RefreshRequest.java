package com.alvarezg.expenses.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank(message = "El refresh token es requerido")
    private String refreshToken;
}