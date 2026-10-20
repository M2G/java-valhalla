package org.example.model.valhalla;

public value class CashAccount {
    private String iban;
    private String otherAccountId;

    public CashAccount(String iban, String otherAccountId) {
        this.iban = iban;
        this.otherAccountId = otherAccountId;
    }

    public String getIban() {
        return iban;
    }

    public String getOtherAccountId() {
        return otherAccountId;
    }

}
