package nangorblast.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void translateMovesOneTileInEachDirection() {
        Position p = new Position(3, 3);
        assertEquals(new Position(3, 2), p.translate(Direction.UP));
        assertEquals(new Position(3, 4), p.translate(Direction.DOWN));
        assertEquals(new Position(2, 3), p.translate(Direction.LEFT));
        assertEquals(new Position(4, 3), p.translate(Direction.RIGHT));
    }

    @Test
    void manhattanDistanceAddsColumnAndRowDifferences() {
        assertEquals(3, new Position(1, 1).manhattanDistance(new Position(3, 2)));
        assertEquals(0, new Position(1, 1).manhattanDistance(new Position(1, 1)));
    }
}
