package com.example.demo.bench;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PromptBenchmarksTest {

    static {
        // Ensure system properties are set BEFORE Spring context starts
        System.setProperty("runBenchmarks", "true");
        System.setProperty("runs", "1");
        System.setProperty("outputFile", "build/bench-results.json");
    }

    @Test
    public void benchmarkProducesReportFile() throws Exception {
        Path p = Path.of("build/bench-results.json");
        // The CommandLineRunner should execute during context startup.
        // Wait briefly for the file to be written.
        int attempts = 0;
        while (attempts < 20 && !Files.exists(p)) {
            Thread.sleep(200);
            attempts++;
        }

        assertTrue(Files.exists(p), "Expected benchmark report at build/bench-results.json");
    }
}
