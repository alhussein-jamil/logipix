package logipix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void missingFileAndExtraArgumentsFailCleanly() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int missing = Main.run(
                new String[] {Path.of("puzzles", "missing.txt").toString()},
                new PrintStream(new ByteArrayOutputStream(), false, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(1, missing);
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("puzzle file not found"));

        ByteArrayOutputStream usage = new ByteArrayOutputStream();
        int extra = Main.run(
                new String[] {"a", "b"},
                new PrintStream(new ByteArrayOutputStream(), false, StandardCharsets.UTF_8),
                new PrintStream(usage, true, StandardCharsets.UTF_8));
        assertEquals(2, extra);
        assertTrue(usage.toString(StandardCharsets.UTF_8).contains("usage:"));
    }
}
