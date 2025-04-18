package com.example.expense.jobs.writer;

import com.example.expense.entity.Expense;
import com.example.expense.repository.ExpenseRepository;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ExpenseWriter {

    // Writes processed data into the database via JPA repository
    @Bean
    public RepositoryItemWriter<Expense> writer(ExpenseRepository repository) {
        RepositoryItemWriter<Expense> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }
}
