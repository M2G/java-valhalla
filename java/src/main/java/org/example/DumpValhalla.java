package org.example;

import org.example.model.valhalla.CashAccount;
import org.example.model.valhalla.EntryDetails;
import org.example.model.valhalla.MonetaryAmount;
import org.example.model.valhalla.Transaction;
import org.example.model.valhalla.TransactionParties;
import org.example.parser.StaxValhallaParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Affiche le contenu extrait par StaxValhallaParser dans le même format texte que DumpLegacy,
 * pour comparer les deux avec diff.
 *
 * Le modèle Valhalla stocke des chaînes vides là où le legacy a null (champ absent) :
 * les chaînes vides sont affichées "null" pour que la comparaison porte sur le contenu.
 */
public class DumpValhalla {

    public static void main(String[] args) throws Exception {
        List<Transaction> txs;
        try (InputStream in = new FileInputStream(args[0])) {
            txs = new StaxValhallaParser().parse(in);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("entries=").append(txs.size()).append('\n');

        for (int i = 0; i < txs.size(); i++) {
            Transaction t = txs.get(i);
            List<EntryDetails> details = t.getEntryDetails();
            sb.append('[').append(i).append("] amt=").append(amt(t.getAmount()))
                    .append(" cd=").append(t.getCreditDebitIndicator())
                    .append(" sts=").append(t.getStatus())
                    .append(" date=").append(t.getBookingDate())
                    .append(" txdtls=").append(details.size())
                    .append('\n');
            for (int j = 0; j < details.size(); j++) {
                EntryDetails d = details.get(j);
                TransactionParties p = d.getRelatedParties();
                CashAccount da = p.getDebtorAccount();
                CashAccount ca = p.getCreditorAccount();
                List<String> un = d.getRemittanceInformation().getUnstructured();
                sb.append('[').append(i).append('.').append(j).append("] e2e=").append(s(d.getTransactionReferencesLegacy()))
                        .append(" txamt=").append(amt(d.getTransactionAmount()))
                        .append(" dbtr=").append(s(p.getDebtorName()))
                        .append(" dbtrIban=").append(s(da.getIban()))
                        .append(" cdtr=").append(s(p.getCreditorName()))
                        .append(" cdtrIban=").append(s(ca.getIban()))
                        .append(" rmt=").append(un.isEmpty() ? "null" : String.join("|", un))
                        .append('\n');
            }
        }
        System.out.print(sb);
    }

    private static String s(String v) {
        return (v == null || v.isEmpty()) ? "null" : v;
    }

    private static String amt(MonetaryAmount m) {
        long minor = m.getMinorUnits();
        String digits = Long.toString(Math.abs(minor));
        while (digits.length() < 3) {
            digits = "0" + digits;
        }
        String text = digits.substring(0, digits.length() - 2) + "." + digits.substring(digits.length() - 2);
        return (minor < 0 ? "-" : "") + text + " " + m.getCurrency();
    }
}
