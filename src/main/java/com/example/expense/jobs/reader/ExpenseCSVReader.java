package com.example.expense.jobs.reader;

import com.example.expense.entity.Expense;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.batch.core.configuration.annotation.StepScope;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Marks this class as a configuration class for Spring to detect and load batch reader setup
@Configuration
public class ExpenseCSVReader {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

    @Autowired
    private MinioClient minioClient;

    // Reads a CSV file from path passed as job parameter
    @Bean
    @StepScope
    public FlatFileItemReader<Expense> expenseReader(@Value("#{jobParameters['filePath']}") String filePath) {
        FlatFileItemReader<Expense> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(2); // Skip title + headers
        reader.setLineMapper(expenseLineMapper());
        return reader;
    }

    //  Read from MinIO Bucket
    @Bean
    @StepScope
    public FlatFileItemReader<Expense> expenseMinioReader(@Value("#{jobParameters['objectName']}") String objectName) throws Exception {
        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("uploads")
                        .object(objectName)
                        .build()
        );

        FlatFileItemReader<Expense> reader = new FlatFileItemReader<>();
        reader.setResource(new InputStreamResource(inputStream));
        reader.setLinesToSkip(2); // Skip title + headers
        reader.setLineMapper(expenseLineMapper());
        return reader;
    }

    @Bean
    @StepScope
    public ItemReader<File> fileReader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new ItemReader<>() {
            private boolean read = false;

            @Override
            public File read() throws Exception {
                if (!read) {
                    read = true;
                    return new File(filePath);
                }
                return null;
            }
        };
    }

    // Mapping CSV lines to Expense objects
    private LineMapper<Expense> expenseLineMapper() {
        DefaultLineMapper<Expense> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(expenseTokenizer());
        lineMapper.setFieldSetMapper(expenseFieldSetMapper());
        return lineMapper;
    }

    // Extract relevant fields from CSV
    private LineTokenizer expenseTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(false);
        tokenizer.setNames("date", "category", "account", "amount",
                "ignore1", "ignore2", "ignore3", "ignore4", "ignore5", "ignore6", "ignore7");
        return tokenizer;
    }

    // Map parsed fields into Expense
    private FieldSetMapper<Expense> expenseFieldSetMapper() {
        return fieldSet -> {
            Expense expense = new Expense();
            expense.setDate(LocalDate.parse(fieldSet.readString("date"), DATE_FORMATTER));
            expense.setCategory(fieldSet.readString("category"));
            expense.setAmount(fieldSet.readBigDecimal("amount"));
            return expense;
        };
    }
}
