package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.dao.enums.TradeRequestMetadata;
import com.verygoodbank.tes.dao.enums.TradeResponseMetadata;
import com.verygoodbank.tes.exception.InternalServerError;
import com.verygoodbank.tes.service.ProductService;
import com.verygoodbank.tes.service.TradeEnrichmentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.verygoodbank.tes.exception.ResponseErrorCode.READING_TRADE_DATA_ERROR;
import static com.verygoodbank.tes.exception.ResponseErrorCode.TRADE_LINE_PROCESSING_ERROR;
import static com.verygoodbank.tes.util.TradeCsvUtils.TRADE_CSV_FORMAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEnrichmentServiceImpl implements TradeEnrichmentService {

    private final ProductService productService;
    private final ExecutorService executorService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${batch-size}")
    private int batchSize;

    @Override
    public List<String> enrichTradeData(final MultipartFile file) {
        List<String> enrichedTrades = new ArrayList<>();
        enrichedTrades.add(getResponseHeader());

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, TRADE_CSV_FORMAT)) {

            List<CSVRecord> batch = new ArrayList<>(batchSize);
            for (CSVRecord csvRecord : csvParser) {
                batch.add(csvRecord);
                if (batch.size() == batchSize) {
                    processBatch(batch, enrichedTrades);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                processBatch(batch, enrichedTrades);
            }
        } catch (IOException e) {
            log.error("Error reading trade data: {}", e.getMessage());
            throw new InternalServerError(READING_TRADE_DATA_ERROR);
        }
        return enrichedTrades;
    }

    private void processBatch(final List<CSVRecord> batch, final List<String> enrichedTrades) {
        List<Future<String>> futures = batch.stream()
                .map(csvRecord -> executorService.submit(() -> enrichTradeLine(csvRecord)))
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
            String date = csvRecord.get(TradeRequestMetadata.DATE.getHeader());
            String productId = csvRecord.get(TradeRequestMetadata.PRODUCT_ID.getHeader());
            String currency = csvRecord.get(TradeRequestMetadata.CURRENCY.getHeader());
            String price = csvRecord.get(TradeRequestMetadata.PRICE.getHeader());

            if (!isValidDate(date)) {
                log.error("Invalid date format in the record: {}", csvRecord);
                return null;
            }

            String productName = productService.getProductName(productId);
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

    private String getResponseHeader() {
        return Arrays.stream(TradeResponseMetadata.values())
                .map(TradeResponseMetadata::getHeader)
                .collect(Collectors.joining(","));
    }
}
