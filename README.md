See the [TASK](./TASK.md) file for instructions.

# Trade Enrichment Service Documentation

## Table of Contents
1. [How to Run the Service](#how-to-run-the-service)
2. [How to Use the API](#how-to-use-the-api)
3. [Limitations of the Code](#limitations-of-the-code)
4. [Design Discussion](#design-discussion)
5. [Ideas for Improvement](#ideas-for-improvement)

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
    - Body: A list of enriched trade data strings
- **Error**: HTTP 4xx or 5xx
    - Body: JSON object with error details

### Example using cURL
```bash
curl -X POST -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/your/trades.csv" \
     http://localhost:8080/api/v1/enrich
```

## Limitations of the Code

1. **Memory Usage**: The current implementation loads all product data into memory. This may not be suitable for extremely large product datasets.

2. **Error Handling**: While basic error handling is implemented, it may not cover all possible edge cases or provide detailed error messages for all scenarios.

3. **Performance**: The service processes trades in batches, which helps with large datasets. However, for very large files (hundreds of millions of trades), it may still face performance issues.

4. **Date Format**: The service assumes a specific date format (yyyyMMdd). It may not handle other date formats correctly.

5. **File Format**: The service assumes a specific CSV format. It may not handle different delimiters or file formats.

6. **Scalability**: The service is designed to run on a single instance. It doesn't support distributed processing out of the box.

## Design Discussion

1. **Spring Boot**: We chose Spring Boot for its ease of use, built-in features, and wide adoption in the Java ecosystem.

2. **Multithreading**: The service uses a thread pool to process trades in parallel, which helps improve performance for large datasets.

3. **Batch Processing**: Trades are processed in batches to manage memory usage and improve efficiency.

4. **In-Memory Product Data**: Product data is loaded into memory for fast lookups. This trades off memory usage for performance.

5. **CSV Parsing**: We use Apache Commons CSV library for parsing CSV data. This provides robust handling of various CSV formats and edge cases, while still maintaining good performance.

6. **Error Handling**: We use Spring's exception handling capabilities to provide consistent error responses.

7. **File Upload**: We leverage Spring's multipart file upload support for handling the trade data file.

## Ideas for Improvement

1. **Database Integration**: Instead of loading product data into memory, use a database (e.g., PostgreSQL) for scalability and to handle larger product datasets.

2. **Distributed Processing**: Implement a distributed processing system (e.g., Apache Spark) to handle extremely large trade datasets.

3. **Caching**: Implement caching for frequently accessed product data to improve performance.

4. **Asynchronous Processing**: For large files, implement an asynchronous processing model where the API returns immediately and provides a job ID for checking the status.

5. **API Versioning**: Implement proper API versioning to ensure backward compatibility as the service evolves.

6. **Comprehensive Logging**: Implement more detailed logging for better monitoring and debugging.

7. **Metrics and Monitoring**: Add metrics collection (e.g., using Micrometer) for monitoring performance and health of the service.

8. **Input Validation**: Implement more robust input validation, possibly using a validation framework like Hibernate Validator.

9. **Security**: Implement authentication and authorization for the API endpoints.

10. **Containerization**: Dockerize the application for easier deployment and scaling.

11. **Configuration Management**: Use external configuration management (e.g., Spring Cloud Config) for easier management of properties across environments.

By addressing these improvements, the Trade Enrichment Service could be made more robust, scalable, and production-ready.
