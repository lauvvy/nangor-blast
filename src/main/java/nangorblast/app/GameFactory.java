package nangorblast.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import nangorblast.engine.CommandHandler;
import nangorblast.engine.GameFlow;
import nangorblast.engine.GameSystem;
import nangorblast.engine.LifeSystem;
import nangorblast.engine.PlayerSystem;
import nangorblast.engine.TimerSystem;
import nangorblast.engine.WinLoseSystem;
import nangorblast.model.GameWorld;

/** The composition root. Each member registers a system with one line in {@link #systems}. */
public final class GameFactory {

    public static final Path KEY_BINDINGS = Path.of("config", "keybindings.properties");

    private GameFactory() {
    }

    /** A new Red Mountain world in the MENU state. */
    public static GameWorld newWorld() {
        return StandInMap.redMountain(new Random());
    }

    /** Every command handler. {@code players} is the same instance passed to {@link #systems}. */
    public static List<CommandHandler> handlers(PlayerSystem players, Runnable onQuit) {
        return List.of(new GameFlow(onQuit), players);
    }

    /** Every system, in tick order (ARCHITECTURE.md, "Order of work inside one tick"). */
    public static List<GameSystem> systems(PlayerSystem players) {
        return List.of(
                players, // 1
                // 2. EnemySystem, then BossSystem
                // 3. BombSystem, then ExplosionSystem
                // 4. HazardSystem
                // 5. PowerUpSystem
                new LifeSystem(), // 5b, after every damage source
                new TimerSystem(), // 6
                new WinLoseSystem() // 7
        );
    }
}
