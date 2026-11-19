package nangorblast.mapio;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import nangorblast.core.EntityKind;
import nangorblast.core.InvalidMapException;
import nangorblast.core.Position;
import nangorblast.model.Entity;
import nangorblast.model.Exit;
import nangorblast.model.Hostile;
import nangorblast.model.Player;
import nangorblast.model.block.DestructibleBlock;
import nangorblast.model.block.IndestructibleBlock;
import nangorblast.model.block.ReinforcedBlock;

/** Creates the initial entities described by the Red Mountain map legend (F07.2). */
public final class EntityFactory {

    private final double playerSpeed;
    private final Function<Position, ? extends Entity> enemyFactory;
    private final Function<Position, ? extends Entity> bossFactory;
    private final Function<Position, ? extends Entity> lavaFactory;

    /**
     * The composition root supplies configured enemy, boss, and lava constructors. Each
     * function must return a fresh, active entity at the requested position. Enemies and
     * bosses must implement {@link Hostile}; lava must start in its dormant state.
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

    /** Floor is implicit in the world, so {@code '.'} produces no entity. */
    public Optional<Entity> create(char symbol, Position position) {
        Objects.requireNonNull(position, "position");
        return switch (symbol) {
            case '.' -> Optional.empty();
            case 'P' -> Optional.of(createPlayer(position));
            case '#' -> Optional.of(new IndestructibleBlock(position));
            case 'D' -> Optional.of(new DestructibleBlock(position));
            case 'R' -> Optional.of(new ReinforcedBlock(position));
            case 'X' -> Optional.of(new Exit(position));
            case 'E' -> Optional.of(createConfigured(symbol, position, enemyFactory, EntityKind.NORMAL_ENEMY));
            case 'B' -> Optional.of(createConfigured(symbol, position, bossFactory, EntityKind.BOSS));
            case 'H' -> Optional.of(createConfigured(symbol, position, lavaFactory, EntityKind.LAVA_DORMANT));
            default -> throw new InvalidMapException("Unknown map character " + describe(symbol) + " at " + position);
        };
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
        return entity;
    }
}
