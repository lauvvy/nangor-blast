package nangorblast.model.bomb;

import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.model.Player;

/** F02.2.3: a remote bomb waits for the detonate action instead of a fuse. */
public final class RemoteBomb extends Bomb {

    public RemoteBomb(Position position, Player owner) {
        super(position, owner, owner.blastRange(), Double.POSITIVE_INFINITY);
    }

    @Override
    public BombType type() {
        return BombType.REMOTE;
    }

    @Override
    public void tickFuse(double dt) {
        // Remote Bombs never explode from the fuse. They wait for the player's detonate action.
    }

    @Override
    public EntityKind kind() {
        return EntityKind.BOMB_REMOTE;
    }
}
