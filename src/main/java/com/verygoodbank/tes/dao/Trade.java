package com.verygoodbank.tes.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
public class Trade {
    private String date;
    private String productId;
    private String currency;
    private double price;
    private String productName; // For enriched data


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Trade trade = (Trade) o;
        return Double.compare(price, trade.price) == 0 && Objects.equals(date, trade.date) && Objects.equals(productId, trade.productId) && Objects.equals(currency, trade.currency) && Objects.equals(productName, trade.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, productId, currency, price, productName);
    }
}
