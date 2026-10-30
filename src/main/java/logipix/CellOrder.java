package logipix;

/** Cells ordered by increasing clue. Equal clues keep their reading order. */
final class CellOrder {
    final int[] clues;
    final int[][] indexes;
    private final int count;

    CellOrder(int height, int width) {
        count = width * height;
        clues = new int[count];
        indexes = new int[count][2];
    }

    void sortByClue() {
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                if (clues[j] < clues[i]) {
                    int clue = clues[j];
                    clues[j] = clues[i];
                    clues[i] = clue;
                    int row = indexes[j][0];
                    int col = indexes[j][1];
                    indexes[j][0] = indexes[i][0];
                    indexes[j][1] = indexes[i][1];
                    indexes[i][0] = row;
                    indexes[i][1] = col;
                }
            }
        }
    }
}
