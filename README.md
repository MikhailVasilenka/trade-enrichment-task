# Trade Enrichment Service Documentation

## Table of Contents
1. [How to Run the Service](#how-to-run-the-service)
2. [How to Use the API](#how-to-use-the-api)
3. [Key Implementation Details](#key-implementation-details)
4. [Limitations of the Code](#limitations-of-the-code)
5. [Design Discussion](#design-discussion)
6. [Ideas for Improvement](#ideas-for-improvement)

## How to Run the Service

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- Ensure you have the `product.csv` file available

### Steps to Run
1. Clone the repository:
   ```
   git clone https://github.com/your-repo/trade-enrichment-service.git
   cd trade-enrichment-service
   ```

2. Update the `application.properties` file:
    - Set the `product.file.path` to the location of your `product.csv` file
    - Adjust `spring.servlet.multipart.max-file-size` and `spring.servlet.multipart.max-request-size` if needed

3. Build the project:
   ```
   mvn clean package
   ```

4. Run the service:
   ```
   java -jar target/trade-enrichment-service-1.0.0.jar
   ```

The service will start and listen on `http://localhost:8080` by default.

## How to Use the API

The service exposes a single endpoint for enriching trade data:

- **Endpoint**: `/api/v1/enrich`
- **Method**: POST
- **Content-Type**: multipart/form-data

### Request
- The request should include a file upload with the key `file`
- The file should be a CSV with the following header:
  ```
  date,product_id,currency,price
  ```

### Response
- **Success**: HTTP 200 OK
    - Body: CSV file containing enriched trade data
- **Error**: HTTP 4xx or 5xx
    - Body: Error details

### Example using cURL
```bash
curl -X POST -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/your/trades.csv" \
     -o enriched_trades.csv \
     http://localhost:8080/api/v1/enrich
```

## Key Implementation Details

1. **Streaming Response**: The service now uses an `OutputStream` to write the enriched trade data directly to the response, improving memory efficiency for large datasets.

2. **Parallel Processing**: The service uses a `ForkJoinPool` to process trades in parallel, enhancing performance for large datasets.

3. **Date Validation Caching**: A `Map` is used as a cache to store validated dates, improving performance by avoiding repeated date parsing for common dates.

4. **Error Handling**: The service includes error handling for various scenarios, including invalid input files and processing errors.

## Limitations of the Code

1. **Memory Usage**: While the streaming response reduces memory usage, the product data is still loaded into memory. This may not be suitable for extremely large product datasets.

2. **Error Handling**: While basic error handling is implemented, it may not cover all possible edge cases or provide detailed error messages for all scenarios.

3. **Date Format**: The service assumes a specific date format (yyyyMMdd). It may not handle other date formats correctly.

4. **File Format**: The service assumes a specific CSV format. It may not handle different delimiters or file formats.

5. **Scalability**: While the service uses parallel processing, it's designed to run on a single instance. It doesn't support distributed processing out of the box.

## Design Discussion

1. **Spring Boot**: We chose Spring Boot for its ease of use, built-in features, and wide adoption in the Java ecosystem.

2. **Streaming Response**: By writing directly to the `OutputStream`, we avoid holding the entire dataset in memory, allowing for processing of larger files.

3. **Parallel Processing**: The use of `ForkJoinPool` allows for efficient parallel processing of trade data, improving performance for large datasets.

4. **In-Memory Product Data**: Product data is loaded into memory for fast lookups. This trades off memory usage for performance.

5. **Date Validation Caching**: By caching validated dates, we reduce the overhead of repeated date parsing, which can significantly improve performance for datasets with many repeated dates.

6. **CSV Parsing**: We use Apache Commons CSV library for parsing CSV data, providing robust handling of various CSV formats and edge cases.

## Ideas for Improvement

1. **Database Integration**: Instead of loading product data into memory, use a database (e.g., PostgreSQL) for scalability and to handle larger product datasets.

2. **Distributed Processing**: Implement a distributed processing system (e.g., Apache Spark) to handle extremely large trade datasets.

3. **API Versioning**: Implement proper API versioning to ensure backward compatibility as the service evolves.

4. **Comprehensive Logging**: Implement more detailed logging for better monitoring and debugging.

5. **Metrics and Monitoring**: Add metrics collection (e.g., using Micrometer) for monitoring performance and health of the service.

6. **Input Validation**: Implement more robust input validation, possibly using a validation framework like Hibernate Validator.

7. **Security**: Implement authentication and authorization for the API endpoints.

8. **Containerization**: Dockerize the application for easier deployment and scaling.

9. **Configuration Management**: Use external configuration management (e.g., Spring Cloud Config) for easier management of properties across environments.

10. **Date Format Flexibility**: Implement support for multiple date formats or allow date format to be specified as a parameter.
