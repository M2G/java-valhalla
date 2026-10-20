package org.example.model.legacy;

public class EntryDetailsLegacy {
    private String transactionReferencesLegacy;
    private MonetaryAmountLegacy transactionAmount;
    private TransactionPartiesLegacy relatedParties;
    private RemittanceInformationLegacy remittanceInformation;

    public EntryDetailsLegacy() {}

    public String getTransactionReferencesLegacy() {
        return transactionReferencesLegacy;
    }

    public void setTransactionReferencesLegacy(String transactionReferencesLegacy) {
        this.transactionReferencesLegacy = transactionReferencesLegacy;
    }

    public void setTransactionAmount(MonetaryAmountLegacy transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public MonetaryAmountLegacy getTransactionAmount() {
        return transactionAmount;
    }

    public TransactionPartiesLegacy getRelatedParties() {
        return relatedParties;
    }

    public void setRelatedParties(TransactionPartiesLegacy relatedParties) {
        this.relatedParties = relatedParties;
    }

    public RemittanceInformationLegacy getRemittanceInformation() {
        return remittanceInformation;
    }

    public void setRemittanceInformation(RemittanceInformationLegacy remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }
}
