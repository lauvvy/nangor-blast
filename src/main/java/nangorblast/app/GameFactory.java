package nangorblast.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import nangorblast.engine.CommandHandler;
import nangorblast.engine.GameFlow;
import nangorblast.engine.GameLoop;
import nangorblast.engine.GameSystem;
import nangorblast.engine.LifeSystem;
import nangorblast.engine.PlayerSystem;
import nangorblast.engine.SnapshotBuilder;
import nangorblast.engine.TimerSystem;
import nangorblast.engine.WinLoseSystem;
import nangorblast.engine.bomb.BombSystem;
import nangorblast.engine.bomb.ExplosionSystem;
import nangorblast.input.Command;
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

    /**
     * The game loop for one session: fresh systems with the menus of {@code flow}. Restart
     * calls this again with a new world, so no system keeps state from the last game.
     */
    public static GameLoop newLoop(GameWorld world, BlockingQueue<Command> commands, GameFlow flow) {
        PlayerSystem players = new PlayerSystem();
        return new GameLoop(world, commands, handlers(players, flow), systems(players),
                GameLoop.DEFAULT_TICKS_PER_SECOND, GameLoop.DEFAULT_MAX_CATCH_UP_TICKS, System::nanoTime,
                w -> SnapshotBuilder.build(w).withPanel(flow.panel()));
    }

    /** Every command handler. {@code players} is the same instance passed to {@link #systems}. */
    public static List<CommandHandler> handlers(PlayerSystem players, GameFlow flow) {
        return List.of(flow, players, new BombSystem());
    }

    /** Every system, in tick order (ARCHITECTURE.md, "Order of work inside one tick"). */
    public static List<GameSystem> systems(PlayerSystem players) {
        return List.of(
                players, // 1
                // 2. EnemySystem, then BossSystem
                new BombSystem(), // 3
                new ExplosionSystem(), // 3b
                // 4. HazardSystem
                // 5. PowerUpSystem
                new LifeSystem(), // 5b, after every damage source
                new TimerSystem(), // 6
                new WinLoseSystem() // 7
        );
    }
}
