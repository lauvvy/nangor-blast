package nangorblast.model.boss;

import java.util.LinkedHashSet;
import java.util.Set;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public final class HeavyPunch implements AttackPattern {
    @Override
    public String name() {
        return "Heavy Punch";
    }

    @Override 
    public boolean isUsable(Boss boss, GameWorld world, BossPhase phase) {
        return true;
    }

    @Override
    public Set<Position> tiles(Boss boss, GameWorld world, BossPhase phase) {
        Set<Position> tiles = new LinkedHashSet<>(AttackTiles.ray(world, boss.position(), boss.facing(), 3));
        if (phase == BossPhase.PHASE_TWO && tiles.size() == 3) {
            for (int aside = -1; aside <= 1; aside += 2) {
                Position tile = AttackTiles.relative(boss, 3, aside);
                if (world.inBounds(tile)) {
                    tiles.add(tile);
                }
            }
        }
        return tiles;
    }

    @Override 
    public double duration(BossPhase phase) {
        return 1.0;
    }

    @Override 
    public boolean grantsVulnerability() {
        return true;
    }
}
