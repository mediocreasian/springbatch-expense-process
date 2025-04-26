package com.example.expense.jobs.writer;

import com.example.expense.dto.ExpenseWithGST;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class GstCsvWriter {
    @Bean
    @StepScope
    public FlatFileItemWriter<ExpenseWithGST> gstWriter() {
        FlatFileItemWriter<ExpenseWithGST> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("output/converted_expenses.csv"));
        writer.setAppendAllowed(false);
        writer.setHeaderCallback(w -> w.write("Date,Amount,GST Rate,GST Amount"));
        // * Writes data per each column of the converted_expense.csv
        writer.setLineAggregator(new DelimitedLineAggregator<>() {{
            setDelimiter(",");
            setFieldExtractor(item -> new Object[] {
                    item.getDate(), item.getAmount(), item.getGstRate(), item.getGstAmount()
            });
        }});
        return writer;
    }
}