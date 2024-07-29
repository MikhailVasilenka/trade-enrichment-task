package com.verygoodbank.tes.util;

import org.apache.commons.csv.CSVFormat;

public class ProductCsvUtils {
    public static final CSVFormat PRODUCT_CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .build();
}
