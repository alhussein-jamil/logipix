package logipix;

import java.util.ArrayList;
import java.util.List;

/**
 * Backtracking Logipix search.
 * Clues are tried in increasing order. Candidates are collected once, then
 * discarded when the partial grid makes them illegal.
 */
final class Solver {
    private final Puzzle puzzle;
    private final List<List<String>> paths;

    Solver(Puzzle puzzle) {
        this.puzzle = puzzle;
        int cells = puzzle.width * puzzle.height;
        paths = new ArrayList<>(cells);
        for (int i = 0; i < cells; i++) {
            paths.add(null);
        }
    }

    static int colorId(int row, int col) {
        return 2 + (row + col) * (1 + row + col) / 2 + col;
    }

    Puzzle solve() {
        excludeAll();
        int order = 0;
        int cells = puzzle.width * puzzle.height;
        while (order < cells && clueAt(order) == 0) {
            order++;
        }
        return search(order, puzzle, null, true);
    }

    private int clueAt(int order) {
        int row = puzzle.ranking.indexes[order][0];
        int col = puzzle.ranking.indexes[order][1];
        return puzzle.entries[row][col];
    }

    private void excludeAll() {
        for (int row = 0; row < puzzle.height; row++) {
            for (int col = 0; col < puzzle.width; col++) {
                if (puzzle.entries[row][col] != 0 && puzzle.colors[row][col][0] >= 0) {
                    markForced(puzzle, row, col, true);
                }
            }
        }
    }

    private Puzzle search(int order, Puzzle board, int[] move, boolean beginning) {
        int cells = board.width * board.height;
        if (order >= cells) {
            return board;
        }
        int row = board.ranking.indexes[order][0];
        int col = board.ranking.indexes[order][1];
        if (!beginning) {
            place(board, row, col, move);
            order++;
            if (order >= cells) {
                return board;
            }
            row = board.ranking.indexes[order][0];
            col = board.ranking.indexes[order][1];
        }
        while (board.entries[row][col] <= 1 || board.colors[row][col][0] <= 0) {
            if (board.entries[row][col] == 1) {
                board.colors[row][col][0] = -1;
                board.direction[row][col] = 6;
                board.colors[row][col][1] = 1;
            }
            order++;
            if (order >= cells) {
                return board;
            }
            row = board.ranking.indexes[order][0];
            col = board.ranking.indexes[order][1];
        }
        for (String encoded : pathsAt(board, row, col)) {
            int[] candidate = decode(encoded);
            if (board.accepts(row, col, candidate)) {
                Puzzle solved = search(order, board.copy(), candidate, false);
                if (solved != null) {
                    return solved;
                }
            }
        }
        return null;
    }

    private void place(Puzzle board, int row, int col, int[] move) {
        int clue = board.entries[row][col];
        int r = row;
        int c = col;
        board.colors[r][c][0] = -1;
        board.colors[r][c][1] = board.entries[r][c];
        reserveAround(r, c, board);
        for (int step = 0; step < clue - 1; step++) {
            c += Directions.COLUMN[move[step]];
            r += Directions.ROW[move[step]];
            if (step < clue - 2) {
                board.direction[r][c] = move[step + 1] + 2;
            } else {
                board.direction[r][c] = 1;
            }
            board.colors[r][c][0] = -1;
            board.colors[r][c][1] = board.entries[r][c];
            reserveAround(r, c, board);
        }
        board.direction[row][col] = move[0] + 7;
    }

    private void reserveAround(int row, int col, Puzzle board) {
        for (int step = 0; step < 4; step++) {
            int r = row + Directions.ROW[step];
            int c = col + Directions.COLUMN[step];
            if (r >= 0 && r < board.height && c >= 0 && c < board.width
                    && board.entries[r][c] != 0
                    && board.colors[r][c][0] > 0) {
                markForced(board, r, c, false);
            }
        }
    }

    private void markForced(Puzzle board, int row, int col, boolean first) {
        if (first) {
            collectPaths(board, row, col);
        }
        int clue = board.entries[row][col];
        if (clue <= 1) {
            board.colors[row][col][0] = -1;
            board.colors[row][col][1] = clue;
            return;
        }
        List<String> matrix = pathsAt(board, row, col);
        int[][] hits = new int[board.height][board.width];
        board.colors[row][col][0] = colorId(row, col);
        board.colors[row][col][1] = clue;
        int stillLegal = matrix.size();
        boolean[] legal = new boolean[stillLegal];
        for (int g = 0; g < matrix.size(); g++) {
            legal[g] = board.accepts(row, col, decode(matrix.get(g)));
            if (!legal[g]) {
                stillLegal--;
            }
        }
        for (int g = 0; g < matrix.size(); g++) {
            int[] move = decode(matrix.get(g));
            int r = row;
            int c = col;
            if (first || legal[g]) {
                for (int step = 0; step < clue - 1; step++) {
                    c += Directions.COLUMN[move[step]];
                    r += Directions.ROW[move[step]];
                    hits[r][c] += 1;
                }
                if (hits[r][c] >= stillLegal) {
                    board.colors[r][c][0] = -colorId(row, col);
                    board.colors[r][c][1] = clue;
                }
            }
        }
    }

    private void collectPaths(Puzzle board, int row, int col) {
        List<String> found = new ArrayList<>();
        paths.set(index(board, row, col), found);
        int clue = board.entries[row][col];
        if (clue <= 1) {
            return;
        }
        int[] movements = new int[clue - 1];
        boolean more = true;
        while (more) {
            board.failedAt = clue - 2;
            if (board.accepts(row, col, movements)) {
                found.add(encode(movements));
            }
            more = Moves.next(movements, board.failedAt);
        }
    }

    private List<String> pathsAt(Puzzle board, int row, int col) {
        return paths.get(index(board, row, col));
    }

    private static int index(Puzzle board, int row, int col) {
        return row * board.width + col;
    }

    private static String encode(int[] movements) {
        StringBuilder encoded = new StringBuilder(movements.length);
        for (int movement : movements) {
            encoded.append(movement);
        }
        return encoded.toString();
    }

    private static int[] decode(String encoded) {
        int[] moves = new int[encoded.length()];
        for (int i = 0; i < encoded.length(); i++) {
            moves[i] = encoded.charAt(i) - '0';
        }
        return moves;
    }
}
