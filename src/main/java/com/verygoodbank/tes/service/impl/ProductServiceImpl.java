package com.verygoodbank.tes.service.impl;

import com.verygoodbank.tes.dao.enums.ProductMetadata;
import com.verygoodbank.tes.exception.InternalServerError;
import com.verygoodbank.tes.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.verygoodbank.tes.exception.ResponseErrorCode.ERROR_LOADING_PRODUCT_DATA;
import static com.verygoodbank.tes.util.ProductCsvUtils.PRODUCT_CSV_FORMAT;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private Map<String, String> productMap;

    @Value("${product.file.path}")
    private String productFilePath;

    @PostConstruct
    public void init() {
        loadProductData();
    }

    @Override
    public String getProductName(String productId) {
        return productMap.getOrDefault(productId, "Missing Product Name");
    }

    private void loadProductData() {
        productMap = new ConcurrentHashMap<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(productFilePath));
             CSVParser csvParser = new CSVParser(reader, PRODUCT_CSV_FORMAT)) {

            for (CSVRecord csvRecord : csvParser) {
                String productId = csvRecord.get(ProductMetadata.PRODUCT_ID.getHeader());
                String productName = csvRecord.get(ProductMetadata.PRODUCT_NAME.getHeader());
                productMap.put(productId, productName);
            }
        } catch (IOException e) {
            log.error("Error loading product data: {}", e.getMessage());
            throw new InternalServerError(ERROR_LOADING_PRODUCT_DATA);
        }
    }
}
