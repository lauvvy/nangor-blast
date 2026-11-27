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
        Set<Position> tiles = new LinkedHashSet<>();
        tiles.add(player);
        tiles.addAll(AttackTiles.ray(world, player, horizontal ? Direction.LEFT : Direction.UP, Integer.MAX_VALUE));
        tiles.addAll(AttackTiles.ray(world, player, horizontal ? Direction.RIGHT : Direction.DOWN, Integer.MAX_VALUE));
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