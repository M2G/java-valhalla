package org.example;

import org.example.model.legacy.TransactionLegacy;
import org.example.model.valhalla.Transaction;
import org.example.parser.StaxLegacyParser;
import org.example.parser.StaxValhallaParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) throws IOException {
        byte[] xmlData;
        try (InputStream xml = TestRunner.class.getResourceAsStream("/sample-camt053.xml")) {
            assert xml != null;
            xmlData = xml.readAllBytes();
        }

        // Legacy parser
        try (ByteArrayInputStream stream1 = new ByteArrayInputStream(xmlData)) {
            StaxLegacyParser legacyParser = new StaxLegacyParser();
            List<TransactionLegacy> legacyResults = legacyParser.parse(stream1);
            System.out.println("Legacy parsed: " + legacyResults.size() + " transactions");
        }

        // Valhalla parser
        try (ByteArrayInputStream stream2 = new ByteArrayInputStream(xmlData)) {
            StaxValhallaParser valhallaParser = new StaxValhallaParser();
            List<Transaction> valhallaResults = valhallaParser.parse(stream2);
            System.out.println("Valhalla parsed: " + valhallaResults.size() + " transactions");
        }
    }
}
