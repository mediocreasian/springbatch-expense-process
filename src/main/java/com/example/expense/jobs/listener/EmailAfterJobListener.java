package com.example.expense.jobs.listener;

import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.mail.internet.MimeMessage;
import org.springframework.batch.core.JobExecution;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Configuration
public class EmailAfterJobListener {

    @Bean
    @Qualifier("emailAfterJobListener")
    @JobScope
    public JobExecutionListener emailAfterJob(@Value("#{jobParameters['email']}") String email,
                                              JavaMailSender mailSender) {

        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                try {
                    Path path = Paths.get("output/converted_expenses.csv");
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    BigDecimal totalGst = BigDecimal.ZERO;
                    List<String> lines = Files.readAllLines(path).subList(1, Files.readAllLines(path).size());

                    for (String line : lines) {
                        String[] parts = line.split(",");
                        totalAmount = totalAmount.add(new BigDecimal(parts[1]));
                        totalGst = totalGst.add(new BigDecimal(parts[3]));
                    }

                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true);
                    helper.setTo(email); // 📨 dynamic email address
                    helper.setSubject("GST Report");
                    helper.setText("Total Amount: " + totalAmount + "\nTotal GST: " + totalGst);
                    helper.addAttachment("converted_expenses.csv", path.toFile());

                    mailSender.send(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
