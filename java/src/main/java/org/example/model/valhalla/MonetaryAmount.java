package org.example.model.valhalla;

public value class MonetaryAmount {
    private final long value; // Храним в минорных единицах (центах, копейках)
    private final String currency;

    public MonetaryAmount(String xmlValue, String currency) {
        // Конвертируем строку типа "1000.50" в минорные единицы (100050)
        this.value = parseToMinorUnits(xmlValue);
        this.currency = currency;
    }

    private static long parseToMinorUnits(String valueStr) {
        try {
            // Упрощённая конвертация
            double d = Double.parseDouble(valueStr);
            return Math.round(d * 100); // 2 знака после запятой
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + valueStr);
        }
    }

    public double getMajorUnits() {
        return value / 100.0;
    }

    public long getMinorUnits() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", getMajorUnits(), currency);
    }
}