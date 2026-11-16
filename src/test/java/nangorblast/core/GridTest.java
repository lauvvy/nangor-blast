package nangorblast.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GridTest {

    @Test
    void initialisesEveryCellAndStoresValues() {
        Grid<String> grid = new Grid<>(3, 2, p -> p.col() + "," + p.row());

        assertEquals("2,1", grid.get(new Position(2, 1)));
        grid.set(new Position(0, 0), "x");
        assertEquals("x", grid.get(new Position(0, 0)));
    }

    @Test
    void checksBounds() {
        Grid<Integer> grid = new Grid<>(3, 2, p -> 0);

        assertTrue(grid.inBounds(new Position(2, 1)));
        assertFalse(grid.inBounds(new Position(3, 0)));
        assertFalse(grid.inBounds(new Position(0, -1)));
        assertThrows(IndexOutOfBoundsException.class, () -> grid.get(new Position(3, 0)));
    }

    @Test
    void refusesEmptySize() {
        assertThrows(IllegalArgumentException.class, () -> new Grid<>(0, 2, p -> 0));
    }
}
