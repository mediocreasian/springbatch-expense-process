package com.example.expense.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
public class FileUploadController {

    private final JobLauncher jobLauncher;
    private final Job importJob;

    public FileUploadController(JobLauncher jobLauncher, Job importJob) {
        this.jobLauncher = jobLauncher;
        this.importJob = importJob;
    }

    // Endpoint to accept uploaded expense CSV
    @PostMapping("/upload-expenses")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        File tempFile = File.createTempFile("upload-", ".csv");
        file.transferTo(tempFile);

        JobParameters params = new JobParametersBuilder()
                .addString("filePath", tempFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis()) // Makes job instance unique
                .toJobParameters();

        jobLauncher.run(importJob, params);

        return ResponseEntity.ok("Batch job started for: " + file.getOriginalFilename());
    }
}
