package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.dao.enums.TradeRequestMetadata;
import com.verygoodbank.tes.dao.enums.TradeResponseMetadata;
import com.verygoodbank.tes.exception.InternalServerError;
import com.verygoodbank.tes.service.ProductService;
import com.verygoodbank.tes.service.TradeEnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static com.verygoodbank.tes.exception.ResponseErrorCode.READING_TRADE_DATA_ERROR;
import static com.verygoodbank.tes.util.TradeCsvUtils.TRADE_CSV_FORMAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEnrichmentServiceImpl implements TradeEnrichmentService {

    private final ProductService productService;
    private final ForkJoinPool forkJoinPool;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final Map<String, Boolean> dateCache = new HashMap<>();

    @Override
    public void enrichTradeData(final MultipartFile file, OutputStream outputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, TRADE_CSV_FORMAT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            writer.write(getResponseHeader());
            writer.newLine();

            forkJoinPool.submit(() ->
                    csvParser.stream()
                            .parallel()
                            .map(this::enrichTradeLine)
                            .filter(Objects::nonNull)
                            .forEach(line -> {
                                try {
                                    writer.write(line);
                                    writer.newLine();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            })
            ).get();

            writer.flush();
        } catch (IOException | InterruptedException | ExecutionException e) {
            log.error("Error processing trade data: {}", e.getMessage());
            throw new InternalServerError(READING_TRADE_DATA_ERROR);
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

            return buildCsvLine(date, productId, productName, currency, price);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid CSV record: " + e.getMessage());
            return null;
        }
    }

    public boolean isValidDate(String date) {
        Boolean cachedResult = dateCache.get(date);
        if (cachedResult != null) {
            return cachedResult;
        }
        boolean isValid;
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            isValid = true;
        } catch (DateTimeParseException e) {
            isValid = false;
        }
        dateCache.put(date, isValid);
        return isValid;
    }

    private String buildCsvLine(String date, String productId, String productName, String currency, String price) {
        final StringBuilder sb = new StringBuilder();
        sb.append(date).append(',')
                .append(productId).append(',')
                .append(productName).append(',')
                .append(currency).append(',')
                .append(price);
        return sb.toString();
    }

    private String getResponseHeader() {
        return Arrays.stream(TradeResponseMetadata.values())
                .map(TradeResponseMetadata::getHeader)
                .collect(Collectors.joining(","));
    }
}
