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
                            Hud hud, GameState state, Panel panel) {

    /** @param panel the open menu, or {@code null} while playing */
    public WorldSnapshot {
        sprites = List.copyOf(sprites);
        warnings = Set.copyOf(warnings);
    }

    /** A snapshot with no menu open. */
    public WorldSnapshot(int width, int height, List<Sprite> sprites, Set<Position> warnings, Hud hud, GameState state) {
        this(width, height, sprites, warnings, hud, state, null);
    }

    public WorldSnapshot withPanel(Panel newPanel) {
        return new WorldSnapshot(width, height, sprites, warnings, hud, state, newPanel);
    }

    /** One entity to draw. */
    public record Sprite(long id, EntityKind kind, Position position) {
    }

    /**
     * A menu drawn over the dimmed map: a title, text lines, numbered items with one selected,
     * and a message for the message line, such as an invalid-input error ("" for none).
     */
    public record Panel(String title, List<String> lines, List<String> items, int selected, String message) {

        public Panel {
            lines = List.copyOf(lines);
            items = List.copyOf(items);
            if (items.isEmpty() || selected < 0 || selected >= items.size()) {
                throw new IllegalArgumentException("Selected item " + selected + " of " + items.size());
            }
        }
    }

    /** The HUD line, and the player's state that changes how the player is drawn. */
    public record Hud(int lives, int score, int secondsLeft, BombType selectedBomb, boolean invulnerable) {
    }
}
