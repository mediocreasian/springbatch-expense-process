
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
    participant Client as Client (e.g. Admin UI / Postman)
    participant Controller as FileUploadController
    participant JobLauncher as JobLauncherService
    participant BatchJob as Spring Batch Job
    participant Reader as CSVReader
    participant Processor as ItemProcessor
    participant Writer as ItemWriter (JPA)
    participant DB as PostgreSQL Database

    Client->>Controller: POST /upload-csv (multipart CSV)
    Controller->>Controller: Save CSV to temp file
    Controller->>JobLauncher: Launch Batch Job with filePath param
    JobLauncher->>BatchJob: importUserJob.run(filePath)

    activate BatchJob
    loop each chunk (e.g. 10 records)
        BatchJob->>Reader: Read line from CSV
        Reader-->>BatchJob: User object
        BatchJob->>Processor: Process User (validate, transform)
        Processor-->>BatchJob: Transformed User
        BatchJob->>Writer: Write to DB
        Writer->>DB: INSERT User
    end
    deactivate BatchJob

    BatchJob-->>JobLauncher: Job completion status
    JobLauncher-->>Controller: Job launched successfully
    Controller-->>Client: 200 OK (Job started)
````


