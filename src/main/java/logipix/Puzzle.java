package logipix;

/** One Logipix board and the marks written while searching. */
final class Puzzle {
    final int width;
    final int height;
    final CellOrder ranking;
    int[][] entries;
    int[][][] colors;
    int[][] direction;
    /** Digit the path enumerator should advance after a rejected path. */
    int failedAt;

    Puzzle(int width, int height, CellOrder ranking) {
        this.width = width;
        this.height = height;
        this.ranking = ranking;
        entries = new int[height][width];
        colors = new int[height][width][2];
        direction = new int[height][width];
    }

    /** Clues never change, so the copy shares that grid. */
    Puzzle copy() {
        Puzzle copy = new Puzzle(width, height, ranking);
        copy.entries = entries;
        copy.colors = copyColors(colors);
        copy.direction = copyGrid(direction);
        return copy;
    }

    /**
     * A path of {@code clue} cells is legal when every step stays on the board,
     * intermediate cells are empty and free, and the last cell holds the same clue.
     */
    boolean accepts(int row, int col, int[] movements) {
        int r = row;
        int c = col;
        int clue = entries[row][col];
        int own = Solver.colorId(row, col);
        int[][][] copies = copyColors(colors);
        copies[row][col][0] = -1;
        copies[row][col][1] = clue;
        for (int step = 0; step < clue - 1; step++) {
            r += Directions.ROW[movements[step]];
            c += Directions.COLUMN[movements[step]];
            boolean inside = c < width && r < height && c >= 0 && r >= 0;
            if (inside
                    && (copies[r][c][0] == 0
                    || copies[r][c][0] == -own
                    || (copies[r][c][1] == clue && copies[r][c][0] != -1))
                    && (entries[r][c] == 0 || (step == clue - 2 && entries[r][c] == clue))) {
                copies[r][c][0] = -1;
                copies[r][c][1] = clue;
            } else {
                failedAt = step;
                return false;
            }
        }
        return entries[r][c] == clue;
    }

    String render() {
        String newline = System.lineSeparator();
        StringBuilder out = new StringBuilder();
        out.append(newline);
        for (String row : glyphRows()) {
            out.append(row).append(newline);
        }
        return out.toString();
    }

    String[] glyphRows() {
        String[] rows = new String[height];
        for (int row = 0; row < height; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < width; col++) {
                line.append(Directions.GLYPH[direction[row][col]]).append("  ");
            }
            rows[row] = line.toString();
        }
        return rows;
    }

    private static int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[grid.length][grid[0].length];
        for (int row = 0; row < grid.length; row++) {
            System.arraycopy(grid[row], 0, copy[row], 0, grid[0].length);
        }
        return copy;
    }

    private static int[][][] copyColors(int[][][] source) {
        int[][][] copy = new int[source.length][source[0].length][2];
        for (int row = 0; row < source.length; row++) {
            for (int col = 0; col < source[0].length; col++) {
                copy[row][col][0] = source[row][col][0];
                copy[row][col][1] = source[row][col][1];
            }
        }
        return copy;
    }
}
