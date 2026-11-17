package nangorblast.model.bomb;

import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Player;

/** F02.2.2: a longer fuse and a larger blast than a Normal Bomb. */
public final class PumpedBomb extends Bomb {

    // Specification default. M-05 moves it into config/game.properties.
    public static final double FUSE_SECONDS = 4.5;
    public static final int MAX_RANGE = 7;

    public PumpedBomb(Position position, Player owner) {
        super(position, owner, Math.min(owner.blastRange() + 2, MAX_RANGE), FUSE_SECONDS);
    }

    @Override
    public BombType type() {
        return BombType.PUMPED;
    }

    @Override
    public int damage() {
        return 2;
    }

    @Override
    public EntityKind kind() {
        return EntityKind.BOMB_PUMPED;
    }
}
