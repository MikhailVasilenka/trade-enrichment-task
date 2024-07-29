package com.verygoodbank.tes.util;

import org.apache.commons.csv.CSVFormat;

public class TradeCsvUtils {
    public static final CSVFormat TRADE_CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();
}
