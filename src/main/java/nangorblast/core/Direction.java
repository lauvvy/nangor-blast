package nangorblast.core;

/** The four movement directions, each with its column and row offset. Rows grow downward. */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dCol;
    private final int dRow;

    Direction(int dCol, int dRow) {
        this.dCol = dCol;
        this.dRow = dRow;
    }

    public int dCol() {
        return dCol;
    }

    public int dRow() {
        return dRow;
    }
}
