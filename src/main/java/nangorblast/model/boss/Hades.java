package nangorblast.model.boss;

import java.util.List;
import nangorblast.core.Position;

public final class Hades extends Boss {
    public Hades(Position position) {
        super(position, List.of(new HeavyPunch(), new SpinAttack(), new BeamAttack(), new GroundSlam()));
    }
}
