package nangorblast.model.bomb;

import nangorblast.core.BombType;
import nangorblast.core.Position;
import nangorblast.model.Player;

/** Factory Method: creates the bomb for a {@link BombType}. The only place a bomb class is chosen. */
public final class BombFactory {

    private BombFactory() {
    }

    public static Bomb create(BombType type, Position position, Player owner) {
        return switch (type) {
            case NORMAL -> new NormalBomb(position, owner);
            // shortcut: Pumped arrives in B-04 and Remote in B-05; until then they cannot be selected.
            case PUMPED, REMOTE -> throw new UnsupportedOperationException(type + " bomb is not implemented yet");
        };
    }
}
