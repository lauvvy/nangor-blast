package nangorblast.model.block;

import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;

/** F04.2.1: never destroyed. */
public final class IndestructibleBlock extends Block {

    public IndestructibleBlock(Position position) {
        super(position);
    }

    @Override
    public boolean isDestroyedBy(BombType type) {
        return false;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.INDESTRUCTIBLE_BLOCK;
    }
}
