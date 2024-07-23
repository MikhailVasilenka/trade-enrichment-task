package com.verygoodbank.tes.util;

import org.apache.commons.csv.CSVFormat;

public class ProductCsvUtils {
    public static final CSVFormat PRODUCT_CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .build();
    public static final String PRODUCT_ID_FIELD_NAME = "product_id";
    public static final String PRODUCT_NAME_FIELD_NAME = "product_name";
}
