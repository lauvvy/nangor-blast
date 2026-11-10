package nangorblast;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import nangorblast.core.EntityKind;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Player;

/**
 * Builds small worlds for tests from rows in the F07.2 legend, for example:
 *
 * <pre>{@code
 * GameWorld world = TestWorlds.fromAscii(
 *         "#####",
 *         "#P..#",
 *         "#####");
 * }</pre>
 *
 * Built in: {@code .} floor, {@code P} player (exactly one), {@code X} exit, and {@code #}
 * as a test wall. Pass a legend to add your own characters or to replace a built-in one,
 * for example {@code Map.of('D', DestructibleBlock::new)}. The world uses a seeded random
 * source, a 300-second timer, and a player speed of 4 tiles per second.
 */
public final class TestWorlds {

    public static final double PLAYER_SPEED = 4.0;
    public static final double TIME_LIMIT = 300.0;

    private TestWorlds() {
    }

    public static GameWorld fromAscii(String... rows) {
        return fromAscii(Map.of(), rows);
    }

    public static GameWorld fromAscii(Map<Character, Function<Position, ? extends Entity>> legend, String... rows) {
        if (rows.length == 0) {
            throw new InvalidMapException("A test world needs at least one row");
        }
        int width = rows[0].length();
        Position spawn = null;
        for (int row = 0; row < rows.length; row++) {
            if (rows[row].length() != width) {
                throw new InvalidMapException("Row " + row + " has " + rows[row].length()
                        + " characters, expected " + width);
            }
            for (int col = 0; col < width; col++) {
                if (rows[row].charAt(col) == 'P' && !legend.containsKey('P')) {
                    if (spawn != null) {
                        throw new InvalidMapException("More than one P in the test world");
                    }
                    spawn = new Position(col, row);
                }
            }
        }
        if (spawn == null) {
            throw new InvalidMapException("The test world has no P");
        }

        GameWorld world = new GameWorld("test", width, rows.length,
                new Player(spawn, 0, PLAYER_SPEED), TIME_LIMIT, new Random(42));
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < width; col++) {
                char c = rows[row].charAt(col);
                Position position = new Position(col, row);
                Function<Position, ? extends Entity> factory = legend.get(c);
                if (factory != null) {
                    world.add(factory.apply(position));
                } else if (c == '#') {
                    world.add(new Wall(position));
                } else if (c == 'X') {
                    world.add(new Exit(position));
                } else if (c != '.' && c != 'P') {
                    throw new InvalidMapException("Unknown character '" + c + "' at " + position
                            + ". Pass it in the legend.");
                }
            }
        }
        return world;
    }

    /** A test stand-in for the Indestructible Block until model/block exists. */
    public static final class Wall extends Entity {

        public Wall(Position position) {
            super(position);
        }

        @Override
        public EntityKind kind() {
            return EntityKind.INDESTRUCTIBLE_BLOCK;
        }

        @Override
        public boolean blocksMovementOf(Entity mover) {
            return true;
        }
    }
}
