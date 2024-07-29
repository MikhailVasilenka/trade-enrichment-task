package com.verygoodbank.tes.dao.enums;

public enum TradeRequestMetadata {
        DATE("date"),
        PRODUCT_ID("product_id"),
        CURRENCY("currency"),
        PRICE("price");

        private final String header;

        TradeRequestMetadata(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }
