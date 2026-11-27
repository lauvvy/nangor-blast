package nangorblast.model.boss;

import java.util.LinkedHashSet;
import java.util.Set;
import nangorblast.core.Direction;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;
import nangorblast.model.block.Block;

final class AttackTiles {
    private AttackTiles() {
    }

    static boolean isBlock(GameWorld world, Position tile) {
        return world.firstAt(tile, Block.class).isPresent();
    }

    static Set<Position> ray(GameWorld world, Position from, Direction direction, int maxLength) {
        Set<Position> tiles = new LinkedHashSet<>();
        Position tile = from;
        for (int step = 0; step < maxLength; step++) {
            tile = tile.translate(direction);
            if (!world.inBounds(tile) || isBlock(world, tile)) {
                break;
            }
            tiles.add(tile);
        }
        return tiles;
    }

    static Position relative(Boss boss, int ahead, int aside) {
        Direction f = boss.facing();
        int col = boss.position().col() + f.dCol() * ahead - f.dRow() * aside;
        int row = boss.position().row() + f.dRow() * ahead + f.dCol() * aside;
        return new Position(col, row);
    }
}
