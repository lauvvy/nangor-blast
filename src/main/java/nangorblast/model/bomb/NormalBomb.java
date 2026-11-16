package nangorblast.model.bomb;

import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Player;

/** F02.2.1: the default bomb. Explodes 3 seconds after placement with the owner's blast range. */
public final class NormalBomb extends Bomb {

    // Specification default. M-05 moves it into config/game.properties.
    public static final double FUSE_SECONDS = 3.0;

    public NormalBomb(Position position, Player owner) {
        super(position, owner, owner.blastRange(), FUSE_SECONDS);
    }

    @Override
    public BombType type() {
        return BombType.NORMAL;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.BOMB_NORMAL;
    }
}
