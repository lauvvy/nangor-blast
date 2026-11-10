package nangorblast.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import nangorblast.core.EventBus;
import nangorblast.core.GameState;
import nangorblast.core.Position;

/**
 * Everything in one game: the F00.1 attributes, the warning tiles, and the event bus.
 * Only the game-loop thread touches it.
 *
 * <p>Entities of every type share one list, and each collection from F00.1 is a typed
 * query: {@code entities(Bomb.class)} is the bombs, {@code entities(Enemy.class)} the
 * enemies, and so on. The queries return copies, so a system may add entities while it
 * iterates. Inactive entities stay in the list until {@link #removeInactive()} runs at the
 * end of the tick.
 */
public final class GameWorld {

    private final String mapName;
    private final int width;
    private final int height;
    private final Player player;
    private final Random random;
    private final EventBus bus = new EventBus();
    private final List<Entity> entities = new ArrayList<>();
    private final Map<Object, Set<Position>> warnings = new HashMap<>();
    private int score;
    private double timeLeft;
    private GameState state = GameState.PLAYING;

    /**
     * @param timeLimit seconds on the game timer at the start (300 in the specification)
     * @param random the only source of randomness in the game; pass a seeded one in tests
     */
    public GameWorld(String mapName, int width, int height, Player player, double timeLimit, Random random) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("World size must be positive, got " + width + "x" + height);
        }
        this.mapName = Objects.requireNonNull(mapName, "mapName");
        this.width = width;
        this.height = height;
        this.player = Objects.requireNonNull(player, "player");
        this.random = Objects.requireNonNull(random, "random");
        this.timeLeft = timeLimit;
        if (!inBounds(player.position())) {
            throw new IllegalArgumentException("Player spawn " + player.position() + " is outside the map");
        }
        entities.add(player);
    }

    public String mapName() {
        return mapName;
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

    public Player player() {
        return player;
    }

    public Random random() {
        return random;
    }

    public EventBus bus() {
        return bus;
    }

    // --- Entities ---

    public void add(Entity entity) {
        if (!inBounds(entity.position())) {
            throw new IllegalArgumentException("Entity " + entity.kind() + " at " + entity.position()
                    + " is outside the map");
        }
        entities.add(entity);
    }

    /** Every active entity, in the order added. */
    public List<Entity> entities() {
        return entities.stream().filter(Entity::isActive).toList();
    }

    /** Every active entity of the given type, including subtypes. */
    public <T extends Entity> List<T> entities(Class<T> type) {
        return entities.stream().filter(Entity::isActive).filter(type::isInstance).map(type::cast).toList();
    }

    public List<Entity> entitiesAt(Position position) {
        return entities.stream().filter(e -> e.isActive() && e.position().equals(position)).toList();
    }

    public <T extends Entity> Optional<T> firstAt(Position position, Class<T> type) {
        return entitiesAt(position).stream().filter(type::isInstance).map(type::cast).findFirst();
    }

    /** True if {@code mover} cannot step onto {@code target}: outside the map, or an entity there blocks it. */
    public boolean isBlockedFor(Entity mover, Position target) {
        if (!inBounds(target)) {
            return true;
        }
        for (Entity other : entities) {
            if (other != mover && other.isActive() && other.position().equals(target)
                    && other.blocksMovementOf(mover)) {
                return true;
            }
        }
        return false;
    }

    /** Called once by the loop at the end of each tick. Systems never remove entities themselves. */
    public void removeInactive() {
        entities.removeIf(e -> !e.isActive());
    }

    // --- Warning tiles (boss attacks, lava) ---

    /** Replaces the warning tiles owned by {@code source}, such as one boss attack or one Lava Tile. */
    public void setWarnings(Object source, Set<Position> tiles) {
        if (tiles.isEmpty()) {
            warnings.remove(source);
        } else {
            warnings.put(source, Set.copyOf(tiles));
        }
    }

    public void clearWarnings(Object source) {
        warnings.remove(source);
    }

    /** The union of every source's warning tiles. */
    public Set<Position> warningTiles() {
        Set<Position> all = new HashSet<>();
        warnings.values().forEach(all::addAll);
        return all;
    }

    // --- Score, timer, state ---

    public int score() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    /** Seconds left on the game timer. */
    public double timeLeft() {
        return timeLeft;
    }

    public void reduceTime(double seconds) {
        timeLeft = Math.max(0, timeLeft - seconds);
    }

    public GameState state() {
        return state;
    }

    public void setState(GameState newState) {
        this.state = Objects.requireNonNull(newState, "newState");
    }
}
