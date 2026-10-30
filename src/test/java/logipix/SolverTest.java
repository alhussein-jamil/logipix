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
    void oddClueCountHasNoSolution() {
        assertNull(solve("""
                2 2
                2 2
                2 0
                """));
    }

    @Test
    void adjacentPairConnects() {
        assertLegal(solve("""
                2 1
                2 2
                """));
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
    void longerClueUsesADetour() {
        Puzzle solved = solve("""
                5 2
                7 0 0 0 7
                0 0 0 0 0
                """);
        assertLegal(solved);
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
    void everyBundledPuzzleIsSolvedWithinASecond() throws IOException {
        int[][] expected = {
                {3, 3}, {6, 6}, {5, 4}, {11, 9}, {20, 20},
                {40, 24}, {25, 25}, {25, 25}, {20, 20}, {30, 35}
        };
        try (PuzzleReader reader = PuzzleReader.open(Path.of("puzzles", "logix.txt"))) {
            for (int index = 0; index < expected.length; index++) {
                assertTrue(reader.hasNext());
                Puzzle puzzle = reader.next();
                assertEquals(expected[index][0], puzzle.width);
                assertEquals(expected[index][1], puzzle.height);
                long start = System.nanoTime();
                Puzzle solved = new Solver(puzzle).solve();
                long millis = (System.nanoTime() - start) / 1_000_000L;
                if (index == 5) {
                    assertNull(solved, "puzzle 6 is an odd set of clues");
                } else {
                    assertNotNull(solved, "puzzle " + (index + 1));
                    assertLegal(solved);
                }
                assertTrue(millis < 1_000L, "puzzle " + (index + 1) + " took " + millis + "ms");
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

    private static void assertLegal(Puzzle puzzle) {
        assertNotNull(puzzle);
        int height = puzzle.height;
        int width = puzzle.width;
        boolean[][] seen = new boolean[height][width];
        int coveredClues = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (puzzle.entries[row][col] != 1) {
                    continue;
                }
                assertEquals(6, puzzle.direction[row][col]);
                seen[row][col] = true;
                coveredClues++;
            }
        }
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int mark = puzzle.direction[row][col];
                if (mark < 7) {
                    continue;
                }
                int clue = puzzle.entries[row][col];
                assertTrue(clue > 1);
                int cursorRow = row;
                int cursorCol = col;
                int dir = mark - 7;
                int length = 1;
                assertFalse(seen[cursorRow][cursorCol]);
                seen[cursorRow][cursorCol] = true;
                while (length < clue) {
                    cursorRow += Directions.ROW[dir];
                    cursorCol += Directions.COLUMN[dir];
                    assertTrue(cursorRow >= 0 && cursorCol >= 0 && cursorRow < height && cursorCol < width);
                    assertFalse(seen[cursorRow][cursorCol]);
                    seen[cursorRow][cursorCol] = true;
                    length++;
                    int next = puzzle.direction[cursorRow][cursorCol];
                    if (length == clue) {
                        assertEquals(1, next);
                        assertEquals(clue, puzzle.entries[cursorRow][cursorCol]);
                    } else {
                        assertEquals(0, puzzle.entries[cursorRow][cursorCol]);
                        assertTrue(next >= 2 && next <= 5);
                        dir = next - 2;
                    }
                }
                coveredClues += 2;
            }
        }
        int positive = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (puzzle.entries[row][col] > 0) {
                    positive++;
                    assertTrue(seen[row][col], "uncovered clue at " + row + "," + col);
                } else if (!seen[row][col]) {
                    assertEquals(0, puzzle.direction[row][col]);
                }
            }
        }
        assertEquals(positive, coveredClues);
    }
}
