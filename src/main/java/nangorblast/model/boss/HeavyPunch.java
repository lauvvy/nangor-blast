package nangorblast.model.boss;

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
        return AttackTiles.ray(world, boss.position(), boss.facing(), 3);
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
