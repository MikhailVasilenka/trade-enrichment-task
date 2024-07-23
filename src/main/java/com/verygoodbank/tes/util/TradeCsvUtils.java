package com.verygoodbank.tes.util;

import org.apache.commons.csv.CSVFormat;

public class TradeCsvUtils {
    public static final CSVFormat TRADE_CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();
    public static final String DATE_FIELD_NAME = "date";
    public static final String PRODUCT_ID_FIELD_NAME = "product_id";
    public static final String CURRENCY_FIELD_NAME = "currency";
    public static final String PRICE_FIELD_NAME = "price";
}
