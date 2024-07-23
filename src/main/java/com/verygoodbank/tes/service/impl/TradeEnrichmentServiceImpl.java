package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.exception.InternalServerError;
import com.verygoodbank.tes.service.TradeEnrichmentService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.verygoodbank.tes.exception.ResponseErrorCode.ERROR_LOADING_PRODUCT_DATA;
import static com.verygoodbank.tes.exception.ResponseErrorCode.READING_TRADE_DATA_ERROR;
import static com.verygoodbank.tes.exception.ResponseErrorCode.TRADE_LINE_PROCESSING_ERROR;
import static com.verygoodbank.tes.util.ProductCsvUtils.PRODUCT_CSV_FORMAT;
import static com.verygoodbank.tes.util.ProductCsvUtils.PRODUCT_NAME_FIELD_NAME;
import static com.verygoodbank.tes.util.TradeCsvUtils.CURRENCY_FIELD_NAME;
import static com.verygoodbank.tes.util.TradeCsvUtils.DATE_FIELD_NAME;
import static com.verygoodbank.tes.util.TradeCsvUtils.PRICE_FIELD_NAME;
import static com.verygoodbank.tes.util.TradeCsvUtils.PRODUCT_ID_FIELD_NAME;
import static com.verygoodbank.tes.util.TradeCsvUtils.TRADE_CSV_FORMAT;

@Slf4j
@Service
public class TradeEnrichmentServiceImpl implements TradeEnrichmentService {

    private Map<String, String> productMap;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${batch-size}")
    private int batchSize;

    @Value("${product.file.path}")
    private String productFilePath;

    @PostConstruct
    public void init() {
        loadProductData(productFilePath);
    }

    @Override
    public List<String> enrichTradeData(final MultipartFile file) {
        List<String> enrichedTrades = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, TRADE_CSV_FORMAT)) {

            List<CSVRecord> batch = new ArrayList<>(batchSize);
            for (CSVRecord csvRecord : csvParser) {
                batch.add(csvRecord);
                if (batch.size() == batchSize) {
                    processBatch(batch, enrichedTrades, executor);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                processBatch(batch, enrichedTrades, executor);
            }
        } catch (IOException e) {
            log.error("Error reading trade data: {}", e.getMessage());
            throw new InternalServerError(READING_TRADE_DATA_ERROR);
        } finally {
            shutdownAndAwaitTermination(executor);
        }
        return enrichedTrades;
    }

    private void processBatch(final List<CSVRecord> batch, final List<String> enrichedTrades, final ExecutorService executor) {
        List<Future<String>> futures = batch.stream()
                .map(csvRecord -> executor.submit(() -> enrichTradeLine(csvRecord)))
                .toList();

        for (Future<String> future : futures) {
            try {
                String enrichedLine = future.get();
                if (enrichedLine != null) {
                    enrichedTrades.add(enrichedLine);
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                log.error("Error processing trade line: {}", e.getMessage());
                throw new InternalServerError(TRADE_LINE_PROCESSING_ERROR);
            }
        }
    }

    private String enrichTradeLine(final CSVRecord csvRecord) {
        try {
            String date = csvRecord.get(DATE_FIELD_NAME);
            String productId = csvRecord.get(PRODUCT_ID_FIELD_NAME);
            String currency = csvRecord.get(CURRENCY_FIELD_NAME);
            String price = csvRecord.get(PRICE_FIELD_NAME);

            if (!isValidDate(date)) {
                log.error("Invalid date format in the record: {}", csvRecord);
                return null;
            }

            String productName = productMap.getOrDefault(productId, "Missing Product Name");
            if (productName.equals("Missing Product Name")) {
                log.error("Missing product mapping for ID: {}", productId);
            }

            return String.join(",", date, productId, productName, currency, price);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid CSV record: " + e.getMessage());
            return null;
        }
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void loadProductData(final String filePath) {
        productMap = new ConcurrentHashMap<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, PRODUCT_CSV_FORMAT)) {

            for (CSVRecord csvRecord : csvParser) {
                String productId = csvRecord.get(PRODUCT_ID_FIELD_NAME);
                String productName = csvRecord.get(PRODUCT_NAME_FIELD_NAME);
                productMap.put(productId, productName);
            }
        } catch (IOException e) {
            log.error("Error loading product data: {}", e.getMessage());
            throw new InternalServerError(ERROR_LOADING_PRODUCT_DATA);
        }
    }

    private void shutdownAndAwaitTermination(final ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
