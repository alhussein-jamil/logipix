package logipix;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads a puzzle file and prints one solution board per puzzle. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        int status = run(args, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length > 1) {
            err.println("usage: logipix [puzzle-file]");
            return 2;
        }
        Path file = args.length == 0 ? Path.of("puzzles", "logix.txt") : Path.of(args[0]);
        if (!Files.isRegularFile(file)) {
            err.println("puzzle file not found: " + file.toAbsolutePath());
            return 1;
        }
        try (PuzzleReader reader = PuzzleReader.open(file)) {
            while (reader.hasNext()) {
                long start = System.nanoTime();
                Puzzle solution = new Solver(reader.next()).solve();
                if (solution == null) {
                    out.println("NO SOLUTION");
                } else {
                    out.print(solution.render());
                }
                long elapsed = (System.nanoTime() - start) / 1_000_000L;
                out.println(elapsed + "ms");
            }
            return 0;
        } catch (IOException | IllegalArgumentException ex) {
            err.println(ex.getMessage());
            return 1;
        }
    }
}
