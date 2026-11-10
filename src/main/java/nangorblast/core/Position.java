package nangorblast.core;

/**
 * An immutable tile position. 0-based, with (0, 0) at the top left.
 * The specification's layouts are 1-based, so its tile (2, 2) is {@code new Position(1, 1)}.
 */
public record Position(int col, int row) {

    public Position translate(Direction direction) {
        return new Position(col + direction.dCol(), row + direction.dRow());
    }

    public int manhattanDistance(Position other) {
        return Math.abs(col - other.col) + Math.abs(row - other.row);
    }
}
