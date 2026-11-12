package nangorblast.model.block;

import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;

/** F04.2.3: destroyed only by a Pumped Bomb's explosion. */
public final class ReinforcedBlock extends Block {

    public ReinforcedBlock(Position position) {
        super(position);
    }

    @Override
    public boolean isDestroyedBy(BombType type) {
        return type == BombType.PUMPED;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.REINFORCED_BLOCK;
    }
}
