package nangorblast.model.block;

import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;

/** F04.2.2: destroyed by any explosion. The power-up it may release comes in B-07. */
public final class DestructibleBlock extends Block {

    public DestructibleBlock(Position position) {
        super(position);
    }

    @Override
    public boolean isDestroyedBy(BombType type) {
        return true;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.DESTRUCTIBLE_BLOCK;
    }
}
