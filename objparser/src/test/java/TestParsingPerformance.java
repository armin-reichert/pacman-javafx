import de.amr.objparser.ObjFileParser;
import org.junit.jupiter.api.*;
import org.tinylog.configuration.Configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestParsingPerformance {

    private final List<Long> timesMillis = new ArrayList<>();

    @BeforeAll
    void setup() throws IOException {
        Configuration.set("level", "off");

        // Warm-up JVM
        URL url = getClass().getResource("/alien_animal/Alien Animal.obj");
        new ObjFileParser(url, StandardCharsets.UTF_8).parse();
    }

    @RepeatedTest(100)
    void testAlienAnimal(RepetitionInfo info) throws IOException {
        URL url = getClass().getResource("/alien_animal/Alien Animal.obj");
        ObjFileParser parser = new ObjFileParser(url, StandardCharsets.UTF_8);

        long start = System.nanoTime();
        parser.parse();
        long millis = (System.nanoTime() - start) / 1_000_000;

        timesMillis.add(millis);
        System.out.println("Run " + info.getCurrentRepetition() + ": " + millis + " ms");
    }

    @AfterAll
    void printSummary() {
        long sum = timesMillis.stream().mapToLong(Long::longValue).sum();
        double avg = sum / (double) timesMillis.size();

        long min = timesMillis.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = timesMillis.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("\n===== Parsing Performance Summary =====");
        System.out.println("Runs: " + timesMillis.size());
        System.out.printf("Average: %.3f ms%n", avg);
        System.out.println("Min: " + min + " ms");
        System.out.println("Max: " + max + " ms");
        System.out.println("=======================================");
    }
}
