package nangorblast.view;

/** Draws snapshots. Implemented by the CLI and the GUI. Holds no game rules. */
@FunctionalInterface
public interface Renderer {

    void render(WorldSnapshot snapshot);
}
