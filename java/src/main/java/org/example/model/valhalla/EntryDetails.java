package org.example.model.valhalla;

public value class EntryDetails {
    private String transactionReferencesLegacy;
    private MonetaryAmount transactionAmount;
    private TransactionParties relatedParties;
    private RemittanceInformation remittanceInformation;


    public EntryDetails(String transactionReferencesLegacy,
                        MonetaryAmount transactionAmount,
                        TransactionParties relatedParties,
                        RemittanceInformation remittanceInformation) {
        this.transactionReferencesLegacy = transactionReferencesLegacy;
        this.transactionAmount = transactionAmount;
        this.relatedParties = relatedParties;
        this.remittanceInformation = remittanceInformation;
    }

    public String getTransactionReferencesLegacy() {
        return transactionReferencesLegacy;
    }

    public MonetaryAmount getTransactionAmount() {
        return transactionAmount;
    }

    public TransactionParties getRelatedParties() {
        return relatedParties;
    }

    public RemittanceInformation getRemittanceInformation() {
        return remittanceInformation;
    }

}
