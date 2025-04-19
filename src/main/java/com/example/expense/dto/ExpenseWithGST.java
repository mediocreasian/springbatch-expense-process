package com.example.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ExpenseWithGST {
    private LocalDate date;
    private BigDecimal amount;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;

}
