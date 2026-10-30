package logipix;

/** Enumerates direction lists used as candidate paths. */
final class Moves {
    private Moves() {
    }

    /**
     * Advance a base-4 direction list, starting at {@code digit}.
     * A step that immediately reverses the previous one is skipped.
     * Returns false once every list has been produced.
     */
    static boolean next(int[] moves, int digit) {
        moves[digit]++;
        int cursor = digit;
        for (int i = digit + 1; i < moves.length; i++) {
            moves[i] = 0;
        }
        while (moves[cursor] > 3) {
            moves[cursor] = 0;
            cursor--;
            if (cursor >= 0) {
                moves[cursor]++;
            } else {
                return false;
            }
        }
        for (int j = 1; j < moves.length; j++) {
            if (Math.abs(moves[j] - moves[j - 1]) == 2) {
                moves[j]++;
                if (moves[j] > 3) {
                    moves[j] = 0;
                    moves[j - 1]++;
                }
                if (Math.abs(moves[j] - moves[j - 1]) == 2) {
                    j--;
                    if (j > 1) {
                        j--;
                    }
                }
            }
        }
        return moves[cursor] <= 3;
    }
}
