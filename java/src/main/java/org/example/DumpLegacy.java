package org.example;

import org.example.model.legacy.EntryDetailsLegacy;
import org.example.model.legacy.MonetaryAmountLegacy;
import org.example.model.legacy.RemittanceInformationLegacy;
import org.example.model.legacy.TransactionLegacy;
import org.example.model.legacy.TransactionPartiesLegacy;
import org.example.parser.StaxLegacyParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Affiche le contenu extrait par StaxLegacyParser dans un format texte canonique,
 * identique à la commande "dump" des versions Rust et C++.
 * Usage : java org.example.DumpLegacy sample-camt053.xml
 */
public class DumpLegacy {

    public static void main(String[] args) throws Exception {
        List<TransactionLegacy> txs;
        try (InputStream in = new FileInputStream(args[0])) {
            txs = new StaxLegacyParser().parse(in);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("entries=").append(txs.size()).append('\n');

        for (int i = 0; i < txs.size(); i++) {
            TransactionLegacy t = txs.get(i);
            List<EntryDetailsLegacy> details = t.getEntryDetails();
            sb.append('[').append(i).append("] amt=").append(amt(t.getAmount()))
                    .append(" cd=").append(t.getCreditDebitIndicator())
                    .append(" sts=").append(t.getStatus())
                    .append(" date=").append(t.getBookingDate())
                    .append(" txdtls=").append(details == null ? "null" : String.valueOf(details.size()))
                    .append('\n');
            if (details == null) {
                continue;
            }
            for (int j = 0; j < details.size(); j++) {
                EntryDetailsLegacy d = details.get(j);
                TransactionPartiesLegacy p = d.getRelatedParties();
                RemittanceInformationLegacy r = d.getRemittanceInformation();
                String dbtrIban = (p == null || p.getDebtorAccount() == null) ? null : p.getDebtorAccount().getIban();
                String cdtrIban = (p == null || p.getCreditorAccount() == null) ? null : p.getCreditorAccount().getIban();
                String rmt = (r == null || r.getUnstructured() == null) ? null : String.join("|", r.getUnstructured());
                sb.append('[').append(i).append('.').append(j).append("] e2e=").append(d.getTransactionReferencesLegacy())
                        .append(" txamt=").append(amt(d.getTransactionAmount()))
                        .append(" dbtr=").append(p == null ? null : p.getDebtorName())
                        .append(" dbtrIban=").append(dbtrIban)
                        .append(" cdtr=").append(p == null ? null : p.getCreditorName())
                        .append(" cdtrIban=").append(cdtrIban)
                        .append(" rmt=").append(rmt)
                        .append('\n');
            }
        }
        System.out.print(sb);
    }

    private static String amt(MonetaryAmountLegacy m) {
        if (m == null) {
            return "null";
        }
        return m.getValue().toPlainString() + " " + m.getCurrency().getCurrencyCode();
    }
}
