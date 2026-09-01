package com.example.javaadvanced.reactorcontext.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequest(

        @NotBlank(message = "productName must be not empty")
        String productName,

        @NotNull(message = "amount must be not empty")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than 0")
        BigDecimal amount
) {
}
