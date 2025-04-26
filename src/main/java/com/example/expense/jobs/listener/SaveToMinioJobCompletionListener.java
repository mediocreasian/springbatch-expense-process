package com.example.expense.jobs.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("saveToMinioJobCompletionListener")
@RequiredArgsConstructor
public class SaveToMinioJobCompletionListener implements JobExecutionListener {

    private final JobLauncher jobLauncher;
    private final Job importJob;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            try {
                String objectName = jobExecution.getJobParameters().getString("objectName");

                JobParameters nextJobParams = new JobParametersBuilder()
                        .addString("objectName", objectName)
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters();

                jobLauncher.run(importJob, nextJobParams);

                System.out.println("saveFileToMinioJob completed. Starting importJob for " + objectName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to launch importJob after saveFileToMinioJob", e);
            }
        }
    }
}
