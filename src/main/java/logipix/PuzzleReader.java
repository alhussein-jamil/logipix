package logipix;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/** Reads concatenated puzzles: width, height, then width*height clues. */
final class PuzzleReader implements AutoCloseable {
    private final Scanner scanner;

    private PuzzleReader(Scanner scanner) {
        this.scanner = scanner;
    }

    static PuzzleReader open(Path path) throws IOException {
        return new PuzzleReader(new Scanner(Files.newBufferedReader(path, StandardCharsets.UTF_8)));
    }

    static PuzzleReader of(String text) {
        return new PuzzleReader(new Scanner(text));
    }

    boolean hasNext() {
        return scanner.hasNextInt();
    }

    Puzzle next() {
        int width = nextNumber("width");
        int height = nextNumber("height");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "puzzle dimensions must be positive, got " + width + "x" + height);
        }
        Puzzle puzzle = new Puzzle(width, height);
        int cells = width * height;
        for (int i = 0; i < cells; i++) {
            if (!scanner.hasNextInt()) {
                throw new IllegalArgumentException(
                        "puzzle " + width + "x" + height + " ended early at cell " + i);
            }
            puzzle.entries[i / width][i % width] = scanner.nextInt();
        }
        return puzzle;
    }

    private int nextNumber(String role) {
        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("expected puzzle " + role);
        }
        return scanner.nextInt();
    }

    @Override
    public void close() {
        scanner.close();
    }
}
