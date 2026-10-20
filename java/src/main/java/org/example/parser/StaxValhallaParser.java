package org.example.parser;


import org.example.model.CreditDebitStatus;
import org.example.model.EntryStatus;
import org.example.model.valhalla.*;
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


public class StaxValhallaParser implements XmlParser<Transaction> {
    @Override
    public List<Transaction> parse(InputStream xmlStream) {
        List<Transaction> transactions = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            NodeList ntryNodes = (NodeList) xpath.compile("//Ntry").evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < ntryNodes.getLength(); i++) {
                Element ntry = (Element) ntryNodes.item(i);
                Transaction tx = parseTransaction(ntry, xpath);
                transactions.add(tx);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML", e);
        }
        return transactions;
    }

    private Transaction parseTransaction(Element ntry, XPath xpath) throws Exception {
        String amountStr = xpath.evaluate("./Amt", ntry);
        String currency = ((Element) xpath.evaluate("./Amt", ntry, XPathConstants.NODE)).getAttribute("Ccy");
        MonetaryAmount amount = new MonetaryAmount(amountStr, currency);

        CreditDebitStatus creditDebitStatus = CreditDebitStatus.valueOf(
            xpath.evaluate("./CdtDbtInd", ntry)
        );

        EntryStatus status = EntryStatus.valueOf(
            xpath.evaluate("./Sts", ntry)
        );

        String bookDateStr = xpath.evaluate("./BookgDt/Dt", ntry);
        LocalDate bookingDate = LocalDate.parse(bookDateStr, DateTimeFormatter.ISO_DATE);

        List<EntryDetails> entryDetails = List.of(); // По умолчанию пустой список
        Element ntryDtlsElement = (Element) xpath.evaluate("./NtryDtls", ntry, XPathConstants.NODE);
        if (ntryDtlsElement != null) {
            entryDetails = parseEntryDetails(ntryDtlsElement, xpath);
        }

        return new Transaction(amount, bookingDate, status, creditDebitStatus, entryDetails);
    }

    private List<EntryDetails> parseEntryDetails(Element ntryDtlsElement, XPath xpath) throws Exception {
        var entryDetails = new ArrayList<EntryDetails>();

        var txDtlsNodes = (NodeList) xpath.evaluate("./TxDtls", ntryDtlsElement, XPathConstants.NODESET);
        for (int i = 0; i < txDtlsNodes.getLength(); i++) {
            var txDtlsElement = (Element) txDtlsNodes.item(i);
            var txDetail = parseTransactionDetails(txDtlsElement, xpath);
            entryDetails.add(txDetail);
        }
        return entryDetails;
    }

    private EntryDetails parseTransactionDetails(Element txDtlsElement, XPath xpath) throws Exception {
        // References
        String endToEndId = xpath.evaluate("./Refs/EndToEndId", txDtlsElement);

        // Amount Details
        Element txAmtElement = (Element) xpath.evaluate("./AmtDtls/TxAmt/Amt", txDtlsElement, XPathConstants.NODE);
        MonetaryAmount transactionAmount = new MonetaryAmount("0", ""); // Default value
        if (txAmtElement != null) {
            String amount = txAmtElement.getTextContent();
            String currency = txAmtElement.getAttribute("Ccy");
            transactionAmount = new MonetaryAmount(amount, currency);
        }

        // Related Parties
        String debtorName = xpath.evaluate("./RltdPties/Dbtr/Nm", txDtlsElement);
        String debtorIban = xpath.evaluate("./RltdPties/DbtrAcct/Id/IBAN", txDtlsElement);
        String creditorName = xpath.evaluate("./RltdPties/Cdtr/Nm", txDtlsElement);
        String creditorIban = xpath.evaluate("./RltdPties/CdtrAcct/Id/IBAN", txDtlsElement);

        CashAccount debtorAccount = new CashAccount(debtorIban, "");
        CashAccount creditorAccount = new CashAccount(creditorIban, "");
        
        TransactionParties parties = new TransactionParties(
            debtorName,
            debtorAccount,
            creditorName,
            creditorAccount
        );

        // Remittance Information
        var remittanceInfo = xpath.evaluate("./RmtInf/Ustrd", txDtlsElement);
                List<String> unstructured = new ArrayList<>();
        if (!remittanceInfo.isEmpty()) {
            unstructured.add(remittanceInfo);
        }
        RemittanceInformation remittance = new RemittanceInformation(unstructured);

        return new EntryDetails(endToEndId,
            transactionAmount,
            parties,
            remittance);
    }
}