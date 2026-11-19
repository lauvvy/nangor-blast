package nangorblast.mapio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import nangorblast.core.EntityKind;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.GameWorld;
import nangorblast.model.Hostile;
import nangorblast.model.Player;
import nangorblast.model.block.DestructibleBlock;
import nangorblast.model.block.IndestructibleBlock;
import nangorblast.model.block.ReinforcedBlock;
import nangorblast.model.map.FloorTile;
import nangorblast.model.map.Tile;

/** Creates the initial entities described by the Red Mountain map legend (F07.2). */
public final class EntityFactory {

    private final double playerSpeed;
    private final Function<Position, ? extends Entity> enemyFactory;
    private final Function<Position, ? extends Entity> bossFactory;
    private final Function<Position, ? extends Entity> lavaFactory;

    /**
     * The composition root supplies configured enemy, boss, and lava constructors. Each
     * function must return a fresh, active entity at the requested position. Enemies and
     * bosses must implement {@link Hostile}; lava must be a {@link Tile} in its dormant state.
     * This keeps map parsing independent of movement strategies and hazard rules.
     */
    public EntityFactory(double playerSpeed,
            Function<Position, ? extends Entity> enemyFactory,
            Function<Position, ? extends Entity> bossFactory,
            Function<Position, ? extends Entity> lavaFactory) {
        if (!Double.isFinite(playerSpeed) || playerSpeed <= 0) {
            throw new IllegalArgumentException("playerSpeed must be finite and positive, got " + playerSpeed);
        }
        this.playerSpeed = playerSpeed;
        this.enemyFactory = Objects.requireNonNull(enemyFactory, "enemyFactory");
        this.bossFactory = Objects.requireNonNull(bossFactory, "bossFactory");
        this.lavaFactory = Objects.requireNonNull(lavaFactory, "lavaFactory");
    }

    /** Player zero with the model's initial lives, bomb capacity, and blast range. */
    public Player createPlayer(Position position) {
        return new Player(position, 0, playerSpeed);
    }

    /** Every supported symbol creates one entity, including a FloorTile for {@code '.'}. */
    public Entity create(char symbol, Position position) {
        Objects.requireNonNull(position, "position");
        return switch (symbol) {
            case '.' -> new FloorTile(position);
            case 'P' -> createPlayer(position);
            case '#' -> new IndestructibleBlock(position);
            case 'D' -> new DestructibleBlock(position);
            case 'R' -> new ReinforcedBlock(position);
            case 'X' -> new Exit(position);
            case 'E' -> createConfigured(symbol, position, enemyFactory, EntityKind.NORMAL_ENEMY);
            case 'B' -> createConfigured(symbol, position, bossFactory, EntityKind.BOSS);
            case 'H' -> createConfigured(symbol, position, lavaFactory, EntityKind.LAVA_DORMANT);
            default -> throw new InvalidMapException("Unknown map character " + describe(symbol) + " at " + position);
        };
    }

    /**
     * Fills a fresh world that contains only its initial player. Terrain and occupants
     * are prepared before any are installed, so a failed creator leaves the world alone.
     * Floor belongs to GameMap; lava is both terrain and a gameplay entity for systems.
     */
    public void populate(GameWorld world, List<String> rows) {
        Objects.requireNonNull(world, "world");
        List<String> copy;
        try {
            copy = MapValidator.copyRows(rows);
            new MapValidator().validate(copy);
        } catch (InvalidMapException ex) {
            throw new InvalidMapException("Map '" + world.mapName() + "': " + ex.getMessage(), ex);
        }
        if (copy.size() != world.height() || copy.get(0).length() != world.width()) {
            throw new InvalidMapException("Map '" + world.mapName() + "': layout dimensions do not match the world");
        }
        if (world.entities().size() != 1 || world.entities().get(0) != world.player()
                || !world.player().position().equals(world.player().spawn())) {
            throw new InvalidMapException("Map '" + world.mapName() + "': populate requires a fresh world with only its player");
        }
        Position spawn = world.player().spawn();
        if (copy.get(spawn.row()).charAt(spawn.col()) != 'P') {
            throw new InvalidMapException("Map '" + world.mapName() + "': P does not match the world's player spawn");
        }
        List<Entity> occupants = new ArrayList<>();
        List<Tile> terrain = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        ids.add(world.player().id());
        for (int row = 0; row < world.height(); row++) {
            for (int col = 0; col < world.width(); col++) {
                Position position = new Position(col, row);
                char symbol = copy.get(row).charAt(col);
                if (symbol == 'P') {
                    terrain.add(new FloorTile(position));
                    continue;
                }
                String context = "Map '" + world.mapName() + "', row " + (row + 1) + ", column " + (col + 1);
                Entity entity;
                try {
                    entity = create(symbol, position);
                } catch (InvalidMapException ex) {
                    throw new InvalidMapException(context + ": " + ex.getMessage(), ex);
                }
                if (!ids.add(entity.id())) {
                    throw new InvalidMapException(context + ": factory reused an entity; return a fresh entity per tile");
                }
                terrain.add(entity instanceof Tile tile ? tile : new FloorTile(position));
                if (!(entity instanceof FloorTile)) {
                    occupants.add(entity);
                }
            }
        }
        terrain.forEach(world.map()::setTile);
        occupants.forEach(world::add);
    }

    static boolean supports(char symbol) {
        return switch (symbol) {
            case '.', 'P', '#', 'D', 'R', 'X', 'E', 'B', 'H' -> true;
            default -> false;
        };
    }

    static String describe(char symbol) {
        return symbol < ' ' || symbol > '~'
                ? "U+%04X".formatted((int) symbol) : "'" + symbol + "'";
    }

    private Entity createConfigured(char symbol, Position position,
            Function<Position, ? extends Entity> creator, EntityKind expectedKind) {
        String context = "Factory for '" + symbol + "' at " + position;
        Entity entity;
        try {
            entity = creator.apply(position);
        } catch (RuntimeException ex) {
            throw new InvalidMapException(context + " failed: " + ex.getMessage(), ex);
        }
        if (entity == null || !entity.isActive()) {
            throw new InvalidMapException(context + " must return an active entity");
        }
        if (!entity.position().equals(position)) {
            throw new InvalidMapException(context + " returned an entity at " + entity.position());
        }
        if (entity.kind() != expectedKind) {
            throw new InvalidMapException(context + " must return " + expectedKind + ", got " + entity.kind());
        }
        if ((symbol == 'E' || symbol == 'B') && !(entity instanceof Hostile)) {
            throw new InvalidMapException(context + " must return a Hostile entity");
        }
        if (symbol == 'H' && !(entity instanceof Tile)) {
            throw new InvalidMapException(context + " must return a Tile");
        }
        return entity;
    }
}
