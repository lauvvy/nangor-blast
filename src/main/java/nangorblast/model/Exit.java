package nangorblast.model;

import nangorblast.core.EntityKind;
import nangorblast.core.Position;

/** The exit tile. Walkable at all times; it opens once every enemy and the boss are defeated. */
public final class Exit extends Entity {

    private boolean open;

    public Exit(Position position) {
        super(position);
    }

    @Override
    public EntityKind kind() {
        return open ? EntityKind.EXIT_OPEN : EntityKind.EXIT_LOCKED;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }
}
