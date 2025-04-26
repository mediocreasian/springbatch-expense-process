package com.example.expense.config;

import com.example.expense.dto.ExpenseWithGST;
import com.example.expense.entity.Expense;

import com.example.expense.jobs.listener.SaveToMinioJobCompletionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

// * Marks this class as a configuration class for Spring to scan and load
@Configuration

// * Enables Spring Batch support and auto-configuration of batch infrastructure
@EnableBatchProcessing
public class BatchConfig {
    // * Job → Step → Reader → Processor → Writer
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    // * ❗ FIX: StepBuilder should NOT be injected
    // * Defines the Step: read -> process -> write -> listen (optional)
    @Bean
    public Step importStep(@Qualifier("expenseMinioReader") ItemReader<Expense> reader,
                           ItemProcessor<Expense, Expense> processor,
                           ItemWriter<Expense> tableWriter) {

        // * Build StepBuilder manually using jobRepository and transactionManager
        return new StepBuilder("importStep", jobRepository)
                .<Expense, Expense>chunk(1000, transactionManager) // * Larger chunk size for batch inserts
                .reader(reader)
                .processor(processor)
                .writer(tableWriter)
                .build();

    }


    // * Defines a Job bean named "importJob"
    @Bean
    public Job importJob(Step importStep) {
        return new JobBuilder("importExpenseJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importStep)
                .build();
    }

    @Bean
    public Step convertStep(@Qualifier("dbReader") ItemReader<Expense> reader,
                            @Qualifier("gstProcessor") ItemProcessor<Expense, ExpenseWithGST> processor,
                            @Qualifier("gstWriter") ItemWriter<ExpenseWithGST> writer) {

        return new StepBuilder("convertStep", jobRepository)
                .<Expense, ExpenseWithGST>chunk(500, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }


    // * * Used to send email of the total amount & the
    @Bean
    public Job convertJob(Step convertStep, @Qualifier("emailAfterJobListener") JobExecutionListener emailListener) {
        return new JobBuilder("convertJob", jobRepository)
                .start(convertStep)
                .listener(emailListener)
                .incrementer(new RunIdIncrementer())
                .build();
    }


    // * 🔹 Step: file -> MinIO
    @Bean
    public Step saveToMinioStep(ItemReader<File> fileReader,
                                ItemWriter<File> minioWriter) {
        return new StepBuilder("saveToMinioStep", jobRepository)
                .<File, File>chunk(1, transactionManager)
                .reader(fileReader)
                .writer(minioWriter)
                .build();
    }

    @Bean
    public Job saveFileToMinioJob(Step saveToMinioStep, SaveToMinioJobCompletionListener listener) {
        return new JobBuilder("saveFileToMinioJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(saveToMinioStep)
                .listener(listener)
                .build();
    }
}
