package org.example.model.legacy;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class MonetaryAmountLegacy {
    private final BigDecimal value;
    private final Currency currency;

    public MonetaryAmountLegacy(BigDecimal value, Currency currency) {
        this.value = Objects.requireNonNull(value);
        this.currency = Objects.requireNonNull(currency);
    }

    public MonetaryAmountLegacy(String valueStr, String currencyCode) {
        this.value = new BigDecimal(valueStr);
        this.currency = Currency.getInstance(currencyCode);
    }

    public BigDecimal getValue() { return value; }

    public Currency getCurrency() { return currency; }

}