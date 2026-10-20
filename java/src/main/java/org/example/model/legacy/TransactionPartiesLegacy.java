package org.example.model.legacy;

public class TransactionPartiesLegacy {
    private String debtorName;
    private CashAccountLegacy debtorAccount;
    private String creditorName;
    private CashAccountLegacy creditorAccount;

    public TransactionPartiesLegacy() {}

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public CashAccountLegacy getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(CashAccountLegacy debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public CashAccountLegacy getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(CashAccountLegacy creditorAccount) {
        this.creditorAccount = creditorAccount;
    }
}
