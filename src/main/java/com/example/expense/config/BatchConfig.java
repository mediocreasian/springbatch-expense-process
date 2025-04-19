package com.example.expense.config;

import com.example.expense.dto.ExpenseWithGST;
import com.example.expense.entity.Expense;

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

// Marks this class as a configuration class for Spring to scan and load
@Configuration

// Enables Spring Batch support and auto-configuration of batch infrastructure
@EnableBatchProcessing
public class BatchConfig {
    // Job → Step → Reader → Processor → Writer
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    // Provides a JobBuilder manually (Spring Batch 5+ requires explicit construction)
    @Bean
    public JobBuilder jobBuilder() {
        return new JobBuilder("importExpenseJob", jobRepository);
    }

    // Defines the Job with a single step
    // Defines a Job bean named "importJob"
    @Bean
    public Job importJob(JobBuilder jobBuilder, Step step1) {
        return jobBuilder
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }

    // ❗ FIX: StepBuilder should NOT be injected
    // Defines the Step: read -> process -> write
    @Bean
    public Step step1(@Qualifier("csvReader") ItemReader<Expense> reader,
                      ItemProcessor<Expense, Expense> processor,
                      ItemWriter<Expense> writer) {

        // Build StepBuilder manually using jobRepository and transactionManager
        return new StepBuilder("step1", jobRepository)
                .<Expense, Expense>chunk(1000, transactionManager) // Larger chunk size for batch inserts
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();

    }


    // * Used to send email of the total amount & the
    @Bean
    public Job convertJob(Step convertStep, JobExecutionListener emailListener) {
        return new JobBuilder("convertJob", jobRepository)
                .start(convertStep)
                .listener(emailListener)
                .incrementer(new RunIdIncrementer())
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
}
