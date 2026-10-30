package logipix;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SolverTest {
    @Test
    void cellsAreTriedInIncreasingClueOrder() {
        try (PuzzleReader reader = PuzzleReader.of("""
                2 2
                4 0
                1 4
                """)) {
            Puzzle puzzle = reader.next();
            assertArrayEquals(new int[] {0, 1}, puzzle.ranking.indexes[0]);
            assertArrayEquals(new int[] {1, 0}, puzzle.ranking.indexes[1]);
            assertArrayEquals(new int[] {0, 0}, puzzle.ranking.indexes[2]);
            assertArrayEquals(new int[] {1, 1}, puzzle.ranking.indexes[3]);
        }
    }

    @Test
    void emptyGridIsSolvedWithoutPaths() {
        assertArrayEquals(new String[] {
                "         ",
                "         ",
                "         "
        }, rows("""
                3 3
                0 0 0
                0 0 0
                0 0 0
                """));
    }

    @Test
    void loneOneIsMarked() {
        assertArrayEquals(new String[] {"@  "}, rows("""
                1 1
                1
                """));
    }

    @Test
    void clueWithNoPartnerHasNoSolution() {
        assertNull(solve("""
                1 1
                2
                """));
    }

    @Test
    void adjacentPairConnects() {
        assertArrayEquals(new String[] {"►  ●  "}, rows("""
                2 1
                2 2
                """));
        assertArrayEquals(new String[] {"▼  ", "●  "}, rows("""
                1 2
                2
                2
                """));
    }

    @Test
    void pathCrossesAnEmptyCell() {
        assertArrayEquals(new String[] {"►  →  ●  "}, rows("""
                3 1
                3 0 3
                """));
    }

    @Test
    void rejectsAShortPuzzleAndANonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (PuzzleReader reader = PuzzleReader.of("2 2\n1\n")) {
                reader.next();
            }
        });
        assertThrows(IllegalArgumentException.class, () -> {
            try (PuzzleReader reader = PuzzleReader.of("0 1\n")) {
                reader.next();
            }
        });
    }

    @Test
    void firstBundledPuzzlesMatchTheOriginalSolver() throws IOException {
        String[][] expected = {
                new String[] {row("..."), row("..."), row("...")},
                new String[] {
                        row("▼►→→→↓"),
                        row("→→↓►●↓"),
                        row("..→●.↓"),
                        row("↓←◄@.↓"),
                        row("→●@..↓"),
                        row(".....●")
                },
                new String[] {
                        row("→●..."),
                        row("▲@●@."),
                        row("..↑◄."),
                        row(".....")
                },
                new String[] {
                        row("►●▼.....↓◄@"),
                        row(".@↓.....↓@."),
                        row("..●@...@●.."),
                        row("...►↓@↓◄..."),
                        row("...▼↓●↓▼..."),
                        row("...↓●↑←↓..."),
                        row("..@●...●▼.."),
                        row(".@▼.....↓▼."),
                        row("●←←.....●●@")
                }
        };
        try (PuzzleReader reader = PuzzleReader.open(Path.of("puzzles", "logix.txt"))) {
            for (String[] board : expected) {
                Puzzle solved = new Solver(reader.next()).solve();
                assertNotNull(solved);
                assertArrayEquals(board, solved.glyphRows());
            }
        }
    }

    @Test
    void bundledFileHasTheOriginalTenPuzzles() throws IOException {
        int[][] expected = {
                {3, 3}, {6, 6}, {5, 4}, {11, 9}, {20, 20},
                {40, 24}, {25, 25}, {25, 25}, {20, 20}, {30, 35}
        };
        try (PuzzleReader reader = PuzzleReader.open(Path.of("puzzles", "logix.txt"))) {
            for (int[] size : expected) {
                assertTrue(reader.hasNext());
                Puzzle puzzle = reader.next();
                assertEquals(size[0], puzzle.width);
                assertEquals(size[1], puzzle.height);
            }
            assertFalse(reader.hasNext());
        }
    }

    private static Puzzle solve(String text) {
        try (PuzzleReader reader = PuzzleReader.of(text)) {
            return new Solver(reader.next()).solve();
        }
    }

    private static String[] rows(String text) {
        return solve(text).glyphRows();
    }

    /** One character per cell. A dot is an empty cell. */
    private static String row(String cells) {
        StringBuilder line = new StringBuilder(cells.length() * 3);
        for (int i = 0; i < cells.length(); i++) {
            char glyph = cells.charAt(i);
            line.append(glyph == '.' ? ' ' : glyph).append("  ");
        }
        return line.toString();
    }
}
