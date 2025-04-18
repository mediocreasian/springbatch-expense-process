
### Author    : Exel 
### Date      : 18 Apr 2025 
#### Note      : This is just for my own personal learning of SpringBatch, I am going to use my Expense CSV File <br> to learn how to process CSV Files and 


#### Folder Structure : 
````
src/main/java/com/example/expense/
├── ExpenseApplication.java                   # Main Spring Boot application entry point
│
├── config/
│   └── BatchConfig.java                      # Core Batch config: defines Job, Step, Reader/Processor/Writer wiring
│
├── controller/
│   └── FileUploadController.java             # Handles file upload requests and triggers batch job via JobLauncher
│
├── dto/                                      # Optional DTOs for response or input (currently unused)
│
├── entity/
│   └── Expense.java                          # JPA entity representing a row in the 'expenses' table
│
├── jobs/                                     # All logic related to the Spring Batch job
│   ├── listener/                             # Optional: listeners that run on job start/finish (e.g., logging, auditing)
│   ├── processor/
│   │   └── ExpenseProcessor.java             # Cleans and transforms the Expense data (e.g., lowercase category)
│   ├── reader/
│   │   └── ExpenseCSVReader.java             # Reads and maps CSV data into Expense objects
│   └── writer/
│       └── ExpenseWriter.java                # Writes Expense objects to DB using a JPA repository
│
├── repository/
│   └── ExpenseRepository.java                # Spring Data JPA repository for CRUD operations on Expense entity
````


#### Sequence Diagram  : 
```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant Controller
    participant JobLauncher
    participant Job
    participant Step
    participant Reader
    participant Processor
    participant Writer
    participant Database

    Client->>Controller: POST /upload-expenses (CSV)
    Controller->>JobLauncher: jobLauncher.run(importJob, JobParameters)
    JobLauncher->>Job: importJob.run()
    Job->>Step: Execute step1
    loop for each chunk (e.g. 1000 records)
        Step->>Reader: expenseReader.read()
        Reader->>Step: Expense[]
        Step->>Processor: process(expense)
        Processor->>Step: processedExpense
        Step->>Writer: write(List<processedExpense>)
        Writer->>Database: JPA bulk insert
    end
    Step->>Job: Step complete
    Job->>JobLauncher: Job complete
    Controller-->>Client: 200 OK: Batch job started
````

To test via MANUAL Curl: 
 curl -X POST -F "file=@expense.csv" http://localhost:8444/upload-expenses


