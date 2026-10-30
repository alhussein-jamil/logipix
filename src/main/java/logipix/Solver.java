package logipix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Covers every clue above 1 with one exact-length path to another equal clue.
 * Paths are generated per pair, then chosen so they never share a cell.
 */
final class Solver {
    private final Puzzle puzzle;
    private final int width;
    private final int height;
    private final int cellCount;
    private final boolean[] used;
    private final int[] moveScore = new int[4];
    private final List<int[]> routes = new ArrayList<>();
    private final List<List<Integer>> touching;
    private int maxClue;

    Solver(Puzzle puzzle) {
        this.puzzle = puzzle;
        width = puzzle.width;
        height = puzzle.height;
        cellCount = width * height;
        used = new boolean[cellCount];
        touching = new ArrayList<>(cellCount);
        for (int cell = 0; cell < cellCount; cell++) {
            touching.add(new ArrayList<>());
        }
    }

    Puzzle solve() {
        int[] tally = tally();
        if (tally == null) {
            return null;
        }
        for (int cell = 0; cell < cellCount; cell++) {
            if (clueAt(cell) == 1) {
                puzzle.direction[row(cell)][col(cell)] = 6;
                used[cell] = true;
            }
        }
        for (int clue = 2; clue <= maxClue; clue++) {
            if (tally[clue] == 0) {
                continue;
            }
            int[] ends = new int[tally[clue]];
            int count = 0;
            for (int cell = 0; cell < cellCount; cell++) {
                if (clueAt(cell) == clue) {
                    ends[count++] = cell;
                }
            }
            for (int i = 0; i < ends.length; i++) {
                for (int j = i + 1; j < ends.length; j++) {
                    collect(ends[i], ends[j], clue - 1);
                }
            }
        }
        return cover() ? puzzle : null;
    }

    private int[] tally() {
        maxClue = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            int clue = clueAt(cell);
            if (clue < 0) {
                return null;
            }
            if (clue > maxClue) {
                maxClue = clue;
            }
        }
        int[] tally = new int[maxClue + 1];
        for (int cell = 0; cell < cellCount; cell++) {
            int clue = clueAt(cell);
            if (clue > 0) {
                tally[clue]++;
            }
        }
        for (int clue = 2; clue <= maxClue; clue++) {
            if ((tally[clue] & 1) != 0) {
                return null;
            }
        }
        return tally;
    }

    private void collect(int start, int target, int steps) {
        int distance = manhattan(start, target);
        if (distance > steps || ((steps - distance) & 1) != 0) {
            return;
        }
        int cap = steps <= 8 ? 4_096 : 512;
        int[] path = new int[steps + 1];
        int[] found = {0};
        path[0] = start;
        used[start] = true;
        gather(start, target, steps, path, 1, cap, found);
        used[start] = false;
    }

    private void gather(
            int cell, int target, int stepsLeft, int[] path, int length, int cap, int[] found) {
        if (found[0] >= cap) {
            return;
        }
        if (stepsLeft == 0) {
            if (cell == target) {
                addRoute(path, length);
                found[0]++;
            }
            return;
        }
        int distance = manhattan(cell, target);
        if (distance > stepsLeft || ((stepsLeft - distance) & 1) != 0) {
            return;
        }
        int[] order = orderedMoves(cell, target);
        for (int i = 0; i < 4 && found[0] < cap; i++) {
            int dir = order[i];
            int nextRow = row(cell) + Directions.ROW[dir];
            int nextCol = col(cell) + Directions.COLUMN[dir];
            if (!canStep(nextRow, nextCol, target, stepsLeft)) {
                continue;
            }
            int next = nextRow * width + nextCol;
            path[length] = next;
            used[next] = true;
            gather(next, target, stepsLeft - 1, path, length + 1, cap, found);
            used[next] = false;
        }
    }

    private void addRoute(int[] path, int length) {
        int[] copy = Arrays.copyOf(path, length);
        int index = routes.size();
        routes.add(copy);
        touching.get(copy[0]).add(index);
        touching.get(copy[length - 1]).add(index);
    }

    private boolean cover() {
        int bestCell = -1;
        int bestCount = Integer.MAX_VALUE;
        for (int cell = 0; cell < cellCount; cell++) {
            if (used[cell] || clueAt(cell) <= 1) {
                continue;
            }
            int choices = 0;
            for (int index : touching.get(cell)) {
                if (fits(routes.get(index))) {
                    choices++;
                }
            }
            if (choices == 0) {
                return false;
            }
            if (choices < bestCount) {
                bestCount = choices;
                bestCell = cell;
            }
        }
        if (bestCell < 0) {
            return true;
        }
        for (int index : touching.get(bestCell)) {
            int[] path = routes.get(index);
            if (!fits(path)) {
                continue;
            }
            take(path);
            if (cover()) {
                return true;
            }
            drop(path);
        }
        return false;
    }

    private boolean fits(int[] path) {
        for (int cell : path) {
            if (used[cell]) {
                return false;
            }
        }
        return true;
    }

    private void take(int[] path) {
        for (int cell : path) {
            used[cell] = true;
        }
        paint(path);
    }

    private void drop(int[] path) {
        for (int cell : path) {
            used[cell] = false;
            puzzle.direction[row(cell)][col(cell)] = 0;
        }
    }

    private void paint(int[] path) {
        puzzle.direction[row(path[0])][col(path[0])] = stepDir(path[0], path[1]) + 7;
        for (int i = 1; i < path.length - 1; i++) {
            puzzle.direction[row(path[i])][col(path[i])] = stepDir(path[i], path[i + 1]) + 2;
        }
        puzzle.direction[row(path[path.length - 1])][col(path[path.length - 1])] = 1;
    }

    private boolean canStep(int row, int col, int target, int stepsLeft) {
        if (row < 0 || col < 0 || row >= height || col >= width) {
            return false;
        }
        int cell = row * width + col;
        if (used[cell]) {
            return false;
        }
        if (cell == target) {
            return stepsLeft == 1;
        }
        return puzzle.entries[row][col] == 0;
    }

    private int[] orderedMoves(int cell, int target) {
        int[] order = {0, 1, 2, 3};
        for (int dir = 0; dir < 4; dir++) {
            int nextRow = row(cell) + Directions.ROW[dir];
            int nextCol = col(cell) + Directions.COLUMN[dir];
            int score = 1_000;
            if (nextRow >= 0 && nextCol >= 0 && nextRow < height && nextCol < width) {
                score = Math.abs(row(target) - nextRow) + Math.abs(col(target) - nextCol);
            }
            moveScore[dir] = score;
        }
        for (int i = 1; i < 4; i++) {
            int dir = order[i];
            int score = moveScore[dir];
            int j = i;
            while (j > 0 && moveScore[order[j - 1]] > score) {
                order[j] = order[j - 1];
                j--;
            }
            order[j] = dir;
        }
        return order;
    }

    private int clueAt(int cell) {
        return puzzle.entries[row(cell)][col(cell)];
    }

    private int manhattan(int from, int to) {
        return Math.abs(row(from) - row(to)) + Math.abs(col(from) - col(to));
    }

    private int stepDir(int from, int to) {
        int drow = row(to) - row(from);
        int dcol = col(to) - col(from);
        for (int dir = 0; dir < 4; dir++) {
            if (Directions.ROW[dir] == drow && Directions.COLUMN[dir] == dcol) {
                return dir;
            }
        }
        throw new IllegalStateException("path step is not orthogonal");
    }

    private int row(int cell) {
        return cell / width;
    }

    private int col(int cell) {
        return cell - row(cell) * width;
    }
}
