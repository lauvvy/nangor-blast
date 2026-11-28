package nangorblast.model.boss;

import java.util.LinkedHashSet;
import java.util.Set;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public final class SpinAttack implements AttackPattern {
    @Override
    public String name() {
        return "Spin Attack";
    }

    @Override 
    public boolean isUsable(Boss boss, GameWorld world, BossPhase phase) {
        return true;
    }

    @Override
    public Set<Position> tiles(Boss boss, GameWorld world, BossPhase phase) {
        int radius = phase == BossPhase.PHASE_TWO ? 2 : 1;
        Set<Position> tiles = new LinkedHashSet<>();
        for (int dCol = -radius; dCol <= radius; dCol++) {
            for (int dRow = -radius; dRow <= radius; dRow++) {
                Position tile = new Position(boss.position().col() + dCol, boss.position().row() + dRow);
                if ((dCol != 0 || dRow != 0) && world.inBounds(tile)) {
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
