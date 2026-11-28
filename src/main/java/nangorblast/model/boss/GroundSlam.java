package nangorblast.model.boss;

import java.util.LinkedHashSet;
import java.util.Set;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public final class GroundSlam implements AttackPattern {
    @Override
    public String name() {
        return "Ground Slam";
    }

    @Override 
    public boolean isUsable(Boss boss, GameWorld world, BossPhase phase) {
        return true;
    }

    @Override
    public Set<Position> tiles(Boss boss, GameWorld world, BossPhase phase) {
        int depth = phase == BossPhase.PHASE_TWO ? 4 : 3;
        Set<Position> tiles = new LinkedHashSet<>();
        for (int ahead = 1; ahead <= depth; ahead++) {
            for (int aside = -1; aside <= 1; aside++) {
                Position tile = AttackTiles.relative(boss, ahead, aside);
                if (world.inBounds(tile)) {
                    tiles.add(tile);
                }
            }
        }
        return tiles;
    }

    @Override 
    public double duration(BossPhase phase) {
        return 1.5;
    }

    @Override 
    public boolean grantsVulnerability() {
        return false;
    }
}
