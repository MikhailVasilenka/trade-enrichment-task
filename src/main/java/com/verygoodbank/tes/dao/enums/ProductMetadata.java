package com.verygoodbank.tes.dao.enums;

public enum ProductMetadata {
        PRODUCT_ID("product_id"),
        PRODUCT_NAME("product_name");

        private final String header;

        ProductMetadata(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }
