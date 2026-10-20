package org.example.model.legacy;

import org.example.model.CreditDebitStatus;
import org.example.model.EntryStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionLegacy {
    private List<EntryDetailsLegacy> entryDetails;
    private MonetaryAmountLegacy amt;
    private LocalDate bookgDt;
    private final List<String> ntryDtls = new ArrayList<>();
    private EntryStatus status;
    private CreditDebitStatus creditDebitIndicator;


    public void setEntryDetails(List<EntryDetailsLegacy> entryDetails) {
        this.entryDetails = entryDetails;
    }

    public List<EntryDetailsLegacy> getEntryDetails() {
        return entryDetails;
    }

    public void setAmount(MonetaryAmountLegacy amt) {
        this.amt = amt;
    }

    public MonetaryAmountLegacy getAmount() {
        return amt;
    }

    public void setBookingDate(LocalDate date) {
        this.bookgDt = date;
    }

    public LocalDate getBookingDate() {
        return bookgDt;
    }

    public void setStatus(EntryStatus status) {
        this.status = status;
    }

    public EntryStatus getStatus() {
        return status;
    }

    public void setCreditDebitIndicator(CreditDebitStatus creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
    }

    public CreditDebitStatus getCreditDebitIndicator() {
        return creditDebitIndicator;
    }
}