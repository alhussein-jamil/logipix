package logipix;

/** One Logipix board and the arrows of a solution. */
final class Puzzle {
    final int width;
    final int height;
    final int[][] entries;
    final int[][] direction;

    Puzzle(int width, int height) {
        this.width = width;
        this.height = height;
        entries = new int[height][width];
        direction = new int[height][width];
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
}
