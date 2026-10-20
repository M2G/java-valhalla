package org.example.benchmark;

import org.example.model.legacy.TransactionLegacy;
import org.example.model.valhalla.Transaction;
import org.example.parser.StaxLegacyParser;
import org.example.parser.StaxValhallaParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 10)
@Fork(2)
public class XmlParserBenchmark {
    private StaxLegacyParser legacyParser;
    private StaxValhallaParser valhallaParser;

    @Setup
    public void setup() {
        this.legacyParser = new StaxLegacyParser();
        this.valhallaParser = new StaxValhallaParser();
    }

    @Benchmark
    public void parseLegacy(Blackhole bh) {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("sample-camt053.xml")) {
            List<TransactionLegacy> result = legacyParser.parse(stream);
            bh.consume(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void parseWithValhalla(Blackhole bh) {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("sample-camt053.xml")) {
            List<Transaction> result = valhallaParser.parse(stream);
            bh.consume(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}