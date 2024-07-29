package com.verygoodbank.tes.dao.enums;

public enum TradeResponseMetadata {
        DATE("date"),
        PRODUCT_ID("product_id"),
        PRODUCT_NAME("product_name"),
        CURRENCY("currency"),
        PRICE("price");

        private final String header;

        TradeResponseMetadata(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }
