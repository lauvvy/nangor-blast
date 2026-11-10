package nangorblast.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;

/**
 * Base of everything placed on the map: player, enemies, blocks, bombs, explosions,
 * power-ups, lava tiles, and the exit. Every entity occupies exactly one tile.
 * To remove an entity, call {@link #deactivate()}; the loop drops it at the end of the tick.
 */
public abstract class Entity {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);

    private final long id = NEXT_ID.getAndIncrement();
    private Position position;
    private boolean active = true;

    protected Entity(Position position) {
        this.position = Objects.requireNonNull(position, "position");
    }

    /** Unique for the lifetime of the program. Renderers use it to follow an entity between frames. */
    public final long id() {
        return id;
    }

    public final Position position() {
        return position;
    }

    public void moveTo(Position target) {
        this.position = Objects.requireNonNull(target, "target");
    }

    public final boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    /** What to draw. May change with the entity's state, as for a Lava Tile or the exit. */
    public abstract EntityKind kind();

    /**
     * Whether this entity stops {@code mover} from stepping onto its tile. Each subclass
     * follows the "Who blocks whom" table in ARCHITECTURE.md. The default is walkable.
     */
    public boolean blocksMovementOf(Entity mover) {
        return false;
    }
}
