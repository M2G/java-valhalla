package org.example.model.valhalla;

import org.example.model.CreditDebitStatus;
import org.example.model.EntryStatus;

import java.time.LocalDate;
import java.util.List;

public value class Transaction {
    private List<EntryDetails> entryDetails;
    private MonetaryAmount amt;
    private LocalDate bookgDt;
    private EntryStatus status;
    private CreditDebitStatus creditDebitIndicator;

    public Transaction(MonetaryAmount amt,
                       LocalDate bookgDt,
                       EntryStatus status,
                       CreditDebitStatus creditDebitIndicator,
                       List<EntryDetails> entryDetails) {
        this.amt = amt;
        this.bookgDt = bookgDt;
        this.status = status;
        this.creditDebitIndicator = creditDebitIndicator;
        this.entryDetails = entryDetails;
    }

    public MonetaryAmount getAmount() {
        return amt;
    }

    public LocalDate getBookingDate() {
        return bookgDt;
    }

    public EntryStatus getStatus() {
        return status;
    }

    public CreditDebitStatus getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public List<EntryDetails> getEntryDetails() {
        return entryDetails;
    }
}