package org.example.model.valhalla;

public value class TransactionParties {
    private String debtorName;
    private CashAccount debtorAccount;
    private String creditorName;
    private CashAccount creditorAccount;

    public TransactionParties(String debtorName,
                              CashAccount debtorAccount,
                              String creditorName,
                              CashAccount creditorAccount) {
        this.debtorName = debtorName;
        this.debtorAccount = debtorAccount;
        this.creditorName = creditorName;
        this.creditorAccount = creditorAccount;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public CashAccount getDebtorAccount() {
        return debtorAccount;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public CashAccount getCreditorAccount() {
        return creditorAccount;
    }

}
