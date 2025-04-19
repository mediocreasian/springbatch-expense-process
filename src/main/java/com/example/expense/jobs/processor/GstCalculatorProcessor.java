package com.example.expense.jobs.processor;

import com.example.expense.dto.ExpenseWithGST;
import com.example.expense.entity.Expense;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

// * Converts each Record and checks for the Year
@Configuration
public class GstCalculatorProcessor {
    @Bean
    public ItemProcessor<Expense, ExpenseWithGST> gstProcessor() {
        return expense -> {
            BigDecimal amount = expense.getAmount();
            int year = expense.getDate().getYear();
            BigDecimal rate = switch (year) {
                case 2024, 2025 -> BigDecimal.valueOf(0.09);
                case 2023 -> BigDecimal.valueOf(0.08);
                case 2022 -> BigDecimal.valueOf(0.07);
                default -> BigDecimal.ZERO;
            };
            BigDecimal gst = amount.multiply(rate);
            return new ExpenseWithGST(expense.getDate(), amount, rate, gst);
        };
    }
}
