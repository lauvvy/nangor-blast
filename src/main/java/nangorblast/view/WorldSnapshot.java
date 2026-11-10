package nangorblast.view;

import java.util.List;
import java.util.Set;
import nangorblast.core.BombType;
import nangorblast.core.EntityKind;
import nangorblast.core.GameState;
import nangorblast.core.Position;

/**
 * An immutable copy of everything a renderer draws, built by the game-loop thread and read
 * by the render thread. Renderers draw only from this; they never see the model.
 */
public record WorldSnapshot(int width, int height, List<Sprite> sprites, Set<Position> warnings,
                            Hud hud, GameState state) {

    public WorldSnapshot {
        sprites = List.copyOf(sprites);
        warnings = Set.copyOf(warnings);
    }

    /** One entity to draw. */
    public record Sprite(long id, EntityKind kind, Position position) {
    }

    /** The HUD line, and the player's state that changes how the player is drawn. */
    public record Hud(int lives, int score, int secondsLeft, BombType selectedBomb, boolean invulnerable) {
    }
}
