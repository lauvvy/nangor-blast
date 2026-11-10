package nangorblast.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** A fixed-size 2D container indexed by {@link Position}, with bounds checks. */
public final class Grid<T> {

    private final int width;
    private final int height;
    private final List<T> cells;

    public Grid(int width, int height, Function<Position, ? extends T> initial) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Grid size must be positive, got " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.cells = new ArrayList<>(width * height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                cells.add(initial.apply(new Position(col, row)));
            }
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean inBounds(Position position) {
        return position.col() >= 0 && position.col() < width
                && position.row() >= 0 && position.row() < height;
    }

    public T get(Position position) {
        return cells.get(index(position));
    }

    public void set(Position position, T value) {
        cells.set(index(position), value);
    }

    private int index(Position position) {
        if (!inBounds(position)) {
            throw new IndexOutOfBoundsException(
                    "Position " + position + " is outside the " + width + "x" + height + " grid");
        }
        return position.row() * width + position.col();
    }
}
