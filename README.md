
### Author    : Exel 
### Date      : 18 Apr 2025 
#### Note      : This is just for my own personal learning of SpringBatch, I am going to use my Expense CSV File <br> to learn how to process CSV Files and 


#### Folder Structure : 
````
src/main/java/com/yourcompany/yourapp/
├── YourAppApplication.java
│
├── config/
│   └── BatchConfig.java              # Spring Batch job/step config
│
├── controller/
│   └── FileUploadController.java     # REST controller for uploading CSV
│
├── dto/
│   └── UploadResultDTO.java          # Optional DTOs for responses
│
├── jobs/                              # <-- Batch-related logic
│   ├── JobLauncherService.java       # Manually triggers batch jobs
│   ├── listener/
│   │   └── JobCompletionListener.java # Runs logic after job ends
│   ├── processor/
│   │   └── UserItemProcessor.java    # Transform input data
│   ├── reader/
│   │   └── UserCSVReader.java        # Reads CSV (can be inline in config)
│   ├── writer/
│   │   └── UserDBWriter.java         # Writes to JPA or repository
│
├── model/
│   └── User.java                     # Your entity mapped to DB
│
├── repository/
│   └── UserRepository.java           # Spring Data JPA repository
│
└── service/
└── UserService.java              # Business logic (e.g., used by controller)
````


#### Sequence Diagram  : 
```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant JobLauncher
    participant Job (importJob)
    participant Step (step1)
    participant Reader
    participant Processor
    participant Writer
    participant Database

    User->>Controller: POST /upload-expenses with CSV
    Controller->>JobLauncher: jobLauncher.run(importJob, params)
    JobLauncher->>Job (importJob): Start Job with filePath param
    Job (importJob)->>Step (step1): Start Step
    loop for each line in CSV
        Step (step1)->>Reader: Read Expense from CSV
        Reader-->>Step (step1): Expense
        Step (step1)->>Processor: Clean/transform Expense
        Processor-->>Step (step1): Processed Expense
        Step (step1)->>Writer: Save Expense
        Writer->>Database: Insert Expense row
    end
    Step (step1)->>Job (importJob): Step completed
    Job (importJob)->>JobLauncher: Job completed
````


