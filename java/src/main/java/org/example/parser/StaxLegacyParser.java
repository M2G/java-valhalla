package org.example.parser;


import org.example.model.*;
import org.example.model.legacy.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StaxLegacyParser implements XmlParser<TransactionLegacy> {

    @Override
    public List<TransactionLegacy> parse(InputStream xmlStream) {
        List<TransactionLegacy> transactions = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            NodeList ntryNodes = (NodeList) xpath.compile("//Ntry").evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < ntryNodes.getLength(); i++) {
                Element ntry = (Element) ntryNodes.item(i);
                TransactionLegacy tx = parseSimpleTransaction(ntry, xpath);
                transactions.add(tx);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML", e);
        }

        return transactions;
    }

    private TransactionLegacy parseSimpleTransaction(Element ntry, XPath xpath) throws Exception {
        TransactionLegacy tx = new TransactionLegacy();

        String amount = xpath.evaluate("./Amt", ntry);
        String currency = ((Element) xpath.evaluate("./Amt", ntry, XPathConstants.NODE)).getAttribute("Ccy");
        tx.setAmount(new MonetaryAmountLegacy(amount, currency));

        String creditDebitStatus = xpath.evaluate("./CdtDbtInd", ntry);
        if (creditDebitStatus != null) {
            tx.setCreditDebitIndicator(CreditDebitStatus.valueOf(creditDebitStatus));
        }
        String statusName = xpath.evaluate("./Sts", ntry);
        if (statusName != null) {
            tx.setStatus(EntryStatus.valueOf(statusName));
        }

        String bookDate = xpath.evaluate("./BookgDt/Dt", ntry);
        if (!bookDate.isEmpty()) {
            tx.setBookingDate(LocalDate.parse(bookDate, DateTimeFormatter.ISO_DATE));
        }

        Element ntryDtlsElement = (Element) xpath.evaluate("./NtryDtls", ntry, XPathConstants.NODE);
        if (ntryDtlsElement != null) {
            List<EntryDetailsLegacy> entryDetails = parseEntryDetails(ntryDtlsElement, xpath);
            tx.setEntryDetails(entryDetails);
        }

        return tx;
    }

    private List<EntryDetailsLegacy> parseEntryDetails(Element ntryDtlsElement, XPath xpath) throws Exception {
        List<EntryDetailsLegacy> entryDetails = new ArrayList<>();

        NodeList txDtlsNodes = (NodeList) xpath.evaluate("./TxDtls", ntryDtlsElement, XPathConstants.NODESET);
        for (int i = 0; i < txDtlsNodes.getLength(); i++) {
            Element txDtlsElement = (Element) txDtlsNodes.item(i);
            EntryDetailsLegacy txDetail = parseTransactionDetails(txDtlsElement, xpath);
            entryDetails.add(txDetail);
        }
        return entryDetails;
    }

    private EntryDetailsLegacy parseTransactionDetails(Element txDtlsElement, XPath xpath) throws Exception {
        EntryDetailsLegacy txDetail = new EntryDetailsLegacy();

        // References
        String endToEndId = xpath.evaluate("./Refs/EndToEndId", txDtlsElement);
        if (!endToEndId.isEmpty()) {
            txDetail.setTransactionReferencesLegacy(endToEndId);
        }

        // Amount Details
        Element txAmtElement = (Element) xpath.evaluate("./AmtDtls/TxAmt/Amt", txDtlsElement, XPathConstants.NODE);
        if (txAmtElement != null) {
            String amount = txAmtElement.getTextContent();
            String currency = txAmtElement.getAttribute("Ccy");
            txDetail.setTransactionAmount(new MonetaryAmountLegacy(amount, currency));
        }

        // Related Parties
        TransactionPartiesLegacy parties = new TransactionPartiesLegacy();

        // Debtor
        String debtorName = xpath.evaluate("./RltdPties/Dbtr/Nm", txDtlsElement);
        if (!debtorName.isEmpty()) {
            parties.setDebtorName(debtorName);
        }

        // Debtor Account
        String debtorIban = xpath.evaluate("./RltdPties/DbtrAcct/Id/IBAN", txDtlsElement);
        if (!debtorIban.isEmpty()) {
            CashAccountLegacy debtorAccount = new CashAccountLegacy();
            debtorAccount.setIban(debtorIban);
            parties.setDebtorAccount(debtorAccount);
        }

        // Creditor
        String creditorName = xpath.evaluate("./RltdPties/Cdtr/Nm", txDtlsElement);
        if (!creditorName.isEmpty()) {
            parties.setCreditorName(creditorName);
        }

        // Creditor Account
        String creditorIban = xpath.evaluate("./RltdPties/CdtrAcct/Id/IBAN", txDtlsElement);
        if (!creditorIban.isEmpty()) {
            CashAccountLegacy creditorAccount = new CashAccountLegacy();
            creditorAccount.setIban(creditorIban);
            parties.setCreditorAccount(creditorAccount);
        }

        // Only set related parties if at least one of the fields is not empty
        if (!debtorName.isEmpty() || !debtorIban.isEmpty() ||
                !creditorName.isEmpty() || !creditorIban.isEmpty()) {
            txDetail.setRelatedParties(parties);
        }

        // Remittance Information
        String remittanceInfo = xpath.evaluate("./RmtInf/Ustrd", txDtlsElement);
        if (!remittanceInfo.isEmpty()) {
            RemittanceInformationLegacy remittance = new RemittanceInformationLegacy();
            List<String> unstructured = new ArrayList<>();
            unstructured.add(remittanceInfo);
            remittance.setUnstructured(unstructured);
            txDetail.setRemittanceInformation(remittance);
        }

        return txDetail;
    }
}