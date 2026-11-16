package nangorblast.app;

import java.util.List;
import java.util.Random;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Hostile;
import nangorblast.model.Player;

/**
 * MVP-0 only: builds the Red Mountain world from the F07.3.3 layout with stand-in entities,
 * so the game runs before the real classes exist. Blocks are walls that do nothing. Enemies
 * and the boss stand still, block, and are {@link Hostile}, so stepping into one costs a life
 * and the exit stays locked. Lava Tiles are walkable and never erupt.
 *
 * <p>shortcut: layout hard-coded and stand-ins instead of real entities. C-09 deletes this
 * class and loads {@code config/maps/red-mountain.txt} through Areli's {@code MapLoader}.
 */
final class StandInMap {

    static final List<String> RED_MOUNTAIN = List.of(
            "##############",
            "#P..D...H...X#",
            "#.##.##.H##E.#",
            "#...H...H....#",
            "###.###.###..#",
            "#DE.H...D....#",
            "#.##.###.##H.#",
            "#...HE...B...#",
            "#E..D....H..E#",
            "##############");

    // Specification values. M-05 moves them into config/game.properties.
    static final double PLAYER_SPEED = 4.0;
    static final double TIME_LIMIT = 300.0;

    private StandInMap() {
    }

    /** A new world in the MENU state. */
    static GameWorld redMountain(Random random) {
        return build(RED_MOUNTAIN, TIME_LIMIT, random);
    }

    static GameWorld build(List<String> rows, double timeLimit, Random random) {
        int width = rows.get(0).length();
        Position spawn = null;
        for (int row = 0; row < rows.size(); row++) {
            int col = rows.get(row).indexOf('P');
            if (col >= 0) {
                spawn = new Position(col, row);
            }
        }
        if (spawn == null) {
            throw new InvalidMapException("The stand-in map has no P");
        }
        GameWorld world = new GameWorld("Red Mountain", width, rows.size(),
                new Player(spawn, 0, PLAYER_SPEED), timeLimit, random);
        for (int row = 0; row < rows.size(); row++) {
            for (int col = 0; col < width; col++) {
                Position at = new Position(col, row);
                switch (rows.get(row).charAt(col)) {
                    case '#' -> world.add(new StandIn(at, EntityKind.INDESTRUCTIBLE_BLOCK, true));
                    case 'D' -> world.add(new StandIn(at, EntityKind.DESTRUCTIBLE_BLOCK, true));
                    case 'E' -> world.add(new HostileStandIn(at, EntityKind.NORMAL_ENEMY));
                    case 'B' -> world.add(new HostileStandIn(at, EntityKind.BOSS));
                    case 'H' -> world.add(new StandIn(at, EntityKind.LAVA_DORMANT, false));
                    case 'X' -> world.add(new Exit(at));
                    case '.', 'P' -> { }
                    default -> throw new InvalidMapException("Unknown character '"
                            + rows.get(row).charAt(col) + "' at " + at);
                }
            }
        }
        world.setState(GameState.MENU);
        return world;
    }

    /** An entity that is drawn and maybe blocks, and does nothing else. */
    static class StandIn extends Entity {
        private final EntityKind kind;
        private final boolean blocks;

        StandIn(Position position, EntityKind kind, boolean blocks) {
            super(position);
            this.kind = kind;
            this.blocks = blocks;
        }

        @Override
        public EntityKind kind() {
            return kind;
        }

        @Override
        public boolean blocksMovementOf(Entity mover) {
            return blocks;
        }
    }

    /** An enemy or boss that stands still. */
    static final class HostileStandIn extends StandIn implements Hostile {
        HostileStandIn(Position position, EntityKind kind) {
            super(position, kind, true);
        }
    }
}
