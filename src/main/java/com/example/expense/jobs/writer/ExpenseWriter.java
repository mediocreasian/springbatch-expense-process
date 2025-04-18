package com.example.expense.jobs.writer;

import com.example.expense.entity.Expense;
import com.example.expense.repository.ExpenseRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ExpenseWriter {

    // Writes processed data into the database via JPA repository
    @Bean
    public ItemWriter<Expense> writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Expense> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
