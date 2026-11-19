package nangorblast.model.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nangorblast.core.EntityKind;
import nangorblast.core.Grid;
import nangorblast.core.Position;

/** The terrain grid. GameWorld separately checks entities occupying its cells. */
public final class GameMap {

    private final String name;
    private final Grid<Tile> tiles;

    public GameMap(String name, int width, int height) {
        this.name = Objects.requireNonNull(name, "name");
        tiles = new Grid<>(width, height, FloorTile::new);
    }

    public String name() {
        return name;
    }

    public int width() {
        return tiles.width();
    }

    public int height() {
        return tiles.height();
    }

    public Tile tileAt(Position position) {
        return tiles.get(position);
    }

    /** Terrain walkability only; blocks and actors are handled by GameWorld.isBlockedFor. */
    public boolean isWalkable(Position position) {
        return tiles.inBounds(position) && tileAt(position).isWalkable();
    }

    /** Used during map population; live terrain is never moved between cells. */
    public void setTile(Tile tile) {
        Objects.requireNonNull(tile, "tile");
        if (!tile.isActive()) {
            throw new IllegalArgumentException("Map terrain must be active at " + tile.position());
        }
        tiles.set(tile.position(), tile);
    }

    /**
     * An immutable snapshot of lava terrain in row order, in any state. The common Tile
     * return type keeps M-01 independent of Marshall's concrete LavaTile from M-03.
     */
    public List<Tile> lavaTiles() {
        List<Tile> lava = new ArrayList<>();
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                Tile tile = tileAt(new Position(col, row));
                EntityKind kind = tile.kind();
                if (kind == EntityKind.LAVA_DORMANT || kind == EntityKind.LAVA_WARNING
                        || kind == EntityKind.LAVA_ERUPTING) {
                    lava.add(tile);
                }
            }
        }
        return List.copyOf(lava);
    }
}
