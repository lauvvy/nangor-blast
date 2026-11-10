package nangorblast.app;

import java.nio.file.Path;
import java.util.List;
import nangorblast.engine.GameSystem;
import nangorblast.engine.TimerSystem;

/** The composition root. Each member registers a system with one line in {@link #systems()}. */
public final class GameFactory {

    public static final Path KEY_BINDINGS = Path.of("config", "keybindings.properties");

    private GameFactory() {
    }

    /** Every system, in tick order (ARCHITECTURE.md, "Order of work inside one tick"). */
    public static List<GameSystem> systems() {
        return List.of(
                // 1. PlayerSystem (C-04)
                // 2. EnemySystem, then BossSystem
                // 3. BombSystem, then ExplosionSystem
                // 4. HazardSystem
                // 5. PowerUpSystem
                new TimerSystem() // 6
                // 7. WinLoseSystem (C-08)
        );
    }
}
