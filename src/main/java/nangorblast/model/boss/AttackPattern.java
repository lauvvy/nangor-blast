package nangorblast.model.boss;

import java.util.Set;
import nangorblast.core.Position;
import nangorblast.model.GameWorld;

public interface AttackPattern {
    String name();

    boolean isUsable(Boss boss, GameWorld world, BossPhase phase);

    Set<Position> tiles(Boss boss, GameWorld world, BossPhase phase);

    double duration(BossPhase phase);

    boolean grantsVulnerability();
}
