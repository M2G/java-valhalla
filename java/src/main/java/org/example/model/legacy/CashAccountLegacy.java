package org.example.model.legacy;

public class CashAccountLegacy {
    private String iban;
    private String otherAccountId;

    public CashAccountLegacy() {}

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getOtherAccountId() {
        return otherAccountId;
    }

    public void setOtherAccountId(String otherAccountId) {
        this.otherAccountId = otherAccountId;
    }
}
