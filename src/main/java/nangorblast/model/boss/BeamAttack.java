package nangorblast.model.boss;

import java.util.LinkedHashSet;
import java.util.Set;
import nangorblast.core.Direction;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public final class BeamAttack implements AttackPattern {
    @Override
    public String name() {
        return "Beam Attack";
    }

    @Override 
    public boolean isUsable(Boss boss, GameWorld world, BossPhase phase) {
        return true;
    }

    @Override
    public Set<Position> tiles(Boss boss, GameWorld world, BossPhase phase) {
        Position player = world.player().position();
        boolean horizontal = boss.facing() == Direction.LEFT || boss.facing() == Direction.RIGHT;
        Direction one = horizontal ? Direction.LEFT : Direction.UP;
        Direction two = horizontal ? Direction.RIGHT : Direction.DOWN;
        int spread = phase == BossPhase.PHASE_TWO ? 1 : 0;
        Set<Position> tiles = new LinkedHashSet<>();
        for (int offset = -spread; offset <= spread; offset++) {
            Position center = horizontal
                ? new Position(player.col(), player.row() + offset)
                : new Position(player.col() + offset, player.row());
            if (!world.inBounds(center) || (offset != 0 && AttackTiles.isBlock(world, center))) {
                continue;
            }
            tiles.add(center);
            tiles.addAll(AttackTiles.ray(world, center, one, Integer.MAX_VALUE));
            tiles.addAll(AttackTiles.ray(world, center, two, Integer.MAX_VALUE));
        }
        return tiles;
    }

    @Override 
    public double duration(BossPhase phase) {
        return 1.0;
    }

    @Override 
    public boolean grantsVulnerability() {
        return false;
    }
}