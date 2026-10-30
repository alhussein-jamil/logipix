package logipix;

/**
 * Grid steps. Index 0 is right, 1 is down, 2 is left, 3 is up.
 * Glyphs: blank, path end, thin arrow, a lone 1, bold path start.
 */
final class Directions {
    static final int[] COLUMN = {1, 0, -1, 0};
    static final int[] ROW = {0, 1, 0, -1};
    static final char[] GLYPH = {' ', '●', '→', '↓', '←', '↑', '@', '►', '▼', '◄', '▲'};

    private Directions() {
    }
}
