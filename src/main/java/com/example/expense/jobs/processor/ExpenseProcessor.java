package com.example.expense.jobs.processor;

import com.example.expense.entity.Expense;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ExpenseProcessor {

    // Simple processor: cleans up category formatting
    @Bean
    public ItemProcessor<Expense, Expense> processor() {
        return item -> {
            item.setCategory(item.getCategory().trim().toLowerCase());
            return item;
        };
    }
}