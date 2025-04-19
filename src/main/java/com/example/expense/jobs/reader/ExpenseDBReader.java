package com.example.expense.jobs.reader;

import com.example.expense.entity.Expense;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;

import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;


// * This Class will be used to read from the Database the amount and the year.
@Configuration

public class ExpenseDBReader {

    @Bean
    @StepScope
    public JpaPagingItemReader<Expense> dbReader(EntityManagerFactory emf) {
        JpaPagingItemReader<Expense> reader = new JpaPagingItemReader<>();
        reader.setQueryString("SELECT e FROM Expense e");
        reader.setEntityManagerFactory(emf);
        reader.setPageSize(100);
        reader.setSaveState(true);
        return reader;
    }
}
