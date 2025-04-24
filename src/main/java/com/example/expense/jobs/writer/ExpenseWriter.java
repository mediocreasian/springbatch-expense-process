package com.example.expense.jobs.writer;

import com.example.expense.entity.Expense;
import io.minio.PutObjectArgs;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


@Configuration
public class ExpenseWriter {

    // Writes processed data into the database via JPA repository
    @Bean
    public ItemWriter<Expense> writer(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Expense> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    // 🔹 Writer: Uploads file to MinIO bucket
    @Bean
    public ItemWriter<File> minioWriter(MinioClient minioClient) {
        return items -> {
            // 🔸 Ensure bucket exists
            String bucketName = "uploads";
            boolean found = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            // 🔸 Upload files
            for (File file : items) {
                try (InputStream input = new FileInputStream(file)) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(file.getName())
                                    .stream(input, file.length(), -1)
                                    .contentType("text/csv")
                                    .build()
                    );
                }
            }
        };
    }
}
