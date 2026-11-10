# ARCHITECTURE — Nangor Blast

> **Status** Initial plan, 10 November 2026. No code exists yet, so everything below is planned, not current.
> **Scope source** [`PRD.md`](PRD.md). **Task status** [`TODO.md`](TODO.md).

The choices in this document were made by the team lead with Claude Code and have not been reviewed by the whole team. Change them here first when the team agrees on something different.

## Technical Stack

| Area | Choice | Notes |
| --- | --- | --- |
| Language | Java 21 LTS | Temurin 21.0.13 is installed on the lead's machine. |
| Build | Gradle 8.14.3 with the wrapper (`gradlew`), Kotlin DSL | Members do not install Gradle. The wrapper checks the distribution's SHA-256. Compiles with `--release 21 -Xlint:all -Werror`. |
| Task runner | `Makefile` calling `gradlew` | Required by the specification. Each member installs `make` (`mingw32-make` on Windows works). Targets: `build`, `run`, `test`, `clean`. `run` starts the installed distribution with `java` so the game gets a real terminal. Confirmation pending, PRD open question 6. |
| Terminal I/O | JLine 3.30.17 (the `org.jline:jline` bundle) | Raw key input and full-screen redraw. Stays on 3.x: it supports Java 21 through its JNI provider. **Unverified on the team's terminals** until the `S-02` results table below is filled. Lanterna is the fallback. |
| GUI | libGDX with the LWJGL3 desktop backend | Chosen by the team lead on 10 November 2026 for sprites, animation, and window scaling. It replaces Swing. It adds dependencies from Maven Central: `gdx`, `gdx-backend-lwjgl3`, the desktop natives, and `gdx-freetype` for fonts. The version is pinned in `G-01`. On macOS the JVM needs `-XstartOnFirstThread`. |
| Logging | SLF4J 2.0.16 with Logback 1.5.12, to `logs/nangor-blast.log` | Never to standard output, which would corrupt the terminal screen. Configured in `src/main/resources/logback.xml`. |
| Tests | JUnit 5.11.3 | Run with `make test`. |
| Hosting | GitHub, `https://github.com/lauvvy/nangor-blast` | Private until the Milestone 2 release. |

### Terminal spike results (S-02)

Each member runs `make spike` in the terminal they will use, follows the three checks on screen, quits with `q`, and fills their row. The spike is `view/cli/TerminalSpike`, built on JLine 3.30.17. A terminal type of `dumb` means JLine could not take control of the terminal, and the game will not work there.

| Member | OS and terminal | JLine type | Keys read without Enter | Flicker at 20 FPS | Key release reported | Two held keys both repeat |
| --- | --- | --- | --- | --- | --- | --- |
| Laura | | | | | | |
| Marshall | | | | | | |
| Ilham | | | | | | |
| Areli | | | | | | |

## System Structure

### Layers

The design goal is Separation of Concerns. Game rules, timing, input, and display live in separate layers, and imports go one way only.

```text
input  ──Command──▶  engine  ──mutates──▶  model
                       │
                       └──WorldSnapshot──▶  view (cli | gui)
```

| Layer | Responsibility | Must not |
| --- | --- | --- |
| `core` | Small shared types: positions, directions, states, the grid, events. | Depend on any other layer. |
| `model` | Game data and rules: entities, tiles, the world. | Do I/O, start threads, read the clock, or import `engine`, `input`, or `view`. |
| `engine` | The game loop and the ordered list of systems. The only code that changes the model. | Draw anything or read keys. |
| `input` | Turns keys into `Command` objects. | Change the model directly. |
| `view` | Draws a `WorldSnapshot`. Two implementations, CLI and GUI. | Change the model or hold game rules. |
| `mapio` | Reads and validates map and configuration files. | Depend on `view` or `engine`. |
| `app` | `Main` and the composition root that wires everything together. | Hold game rules. |

This is MVC: `model` is the model, `view` is the view, and `engine` with `input` is the controller.

### Packages and owners

One owner per package. A member changes another member's package only with that owner's agreement.

```text
src/main/java/nangorblast/
  app/                 Main, GameFactory (composition root), GameSession Laura
  core/                Position, Direction, GameState, BombType, Grid<T>,
                       EntityKind, GameEvent, EventBus, events,
                       exceptions                                        Laura
  model/               GameWorld, Entity, Damageable, Damage,
                       DamageSource, Player, Exit                        Laura
  model/bomb/          Bomb, NormalBomb, PumpedBomb, RemoteBomb,
                       BombFactory                                       Marshall
  model/explosion/     Explosion                                         Marshall
  model/block/         Block, IndestructibleBlock, DestructibleBlock,
                       ReinforcedBlock                                   Marshall
  model/powerup/       PowerUp, PowerUpType                              Marshall
  model/enemy/         Enemy, NormalEnemy, MovementBehavior,
                       PatrolBehavior, ChaseBehavior                     Ilham
  model/boss/          Boss, Hades, AttackPattern and its four
                       implementations, BossPhase                        Ilham
  model/map/           GameMap, Tile, FloorTile, LavaTile, LavaState     Areli
  mapio/               MapLoader, MapValidator, EntityFactory,
                       GameConfig                                        Areli
  editor/              Map Maker (bonus B03)                             Areli
  engine/              GameLoop, GameSystem, CommandHandler, RenderLoop,
                       TimerSystem, SnapshotBuilder, PlayerSystem,
                       WinLoseSystem                                     Laura
  engine/bomb/         BombSystem, ExplosionSystem, PowerUpSystem,
                       ScoreSystem                                       Marshall
  engine/enemy/        EnemySystem, BossSystem                           Ilham
  engine/hazard/       HazardSystem                                      Areli
  input/               Action, Command, InputSource, KeyBindings         Laura
  view/                Renderer, WorldSnapshot                           Laura
  view/cli/            CliRenderer, CliInput, menu screens               Laura
  view/gui/            GuiRenderer, GuiInput, libGDX launcher (bonus B01) Laura
src/test/java/nangorblast/   Mirrors the packages above. Each owner tests their own.
config/
  maps/red-mountain.txt      The map, in the F07.2 legend                Areli
  game.properties            Tunable values (timer, speeds, durations)   Areli
  keybindings.properties     Keys for each action, per player            Laura
assets/                      Sprites and the font for the GUI            Laura
docs/                        Project documents, specification, activity log
doc/                         Submission PDFs. The specification requires this
                             folder name. Created in S-01.
```

The only file every member edits is `app/GameFactory`. Each member adds one line there to register their systems. Keep those edits to one line per system so merges stay trivial.

### Contracts

`C-01` delivers these types first. Every other task is written against them, so no member waits for another member's implementation. The names are fixed here. The signatures settled in `C-01` follow the table.

| Type | Package | Purpose |
| --- | --- | --- |
| `Position` | `core` | Immutable `(col, row)`, 0-based, `(0, 0)` at the top left. The specification's layout is 1-based. |
| `Direction` | `core` | `UP`, `DOWN`, `LEFT`, `RIGHT`, each with a column and row offset. |
| `GameState` | `core` | `MENU`, `PLAYING`, `PAUSED`, `WIN`, `LOSE`. |
| `Grid<T>` | `core` | Fixed-size 2D container with bounds checks. |
| `EntityKind` | `core` | One constant per thing that can be drawn. Renderers map it to a glyph or sprite, so the model never knows how it looks. |
| `GameEvent`, `EventListener<E extends GameEvent>`, `EventBus` | `core` | Publish and subscribe. Used for score, HUD messages, and logging. |
| `GameException` and subclasses | `core` | `InvalidMapException`, `ConfigLoadException`, `InvalidActionException`. |
| `Entity` | `model` | Abstract base with `position`, `active`, and `kind()`. |
| `Damageable`, `Damage` | `model` | Anything that can be hit. `Damage` carries the amount and an activation ID so one activation hurts a target once. |
| `Player` | `model` | F01.1 attributes, plus capped mutators for power-ups. |
| `GameWorld` | `model` | The F00.1 attributes, plus the warning tiles and the event bus. |
| `GameSystem` | `engine` | `update(GameWorld world, double dt)`. One class per concern. |
| `Action`, `Command` | `input` | `Action` names every action. A `Command` is one action by one player. It carries the player index so Local Multiplayer needs no redesign. |
| `InputSource` | `input` | Produces commands from a device. Implemented by CLI and GUI. |
| `Renderer`, `WorldSnapshot` | `view` | The snapshot is an immutable copy of what to draw. |
| `TestWorlds.fromAscii(String...)` | test | Builds a small world from legend characters. Every member's tests use it. |

#### Signatures (settled in C-01)

```java
// core
record Position(int col, int row) { Position translate(Direction); int manhattanDistance(Position); }
enum Direction { UP, DOWN, LEFT, RIGHT; int dCol(); int dRow(); }
enum GameState { MENU, PLAYING, PAUSED, WIN, LOSE }
enum BombType { NORMAL, PUMPED, REMOTE }          // in core: Player, HUD, and bombs share it
enum EntityKind { INDESTRUCTIBLE_BLOCK, ..., POWER_UP_HEART }   // one per row in DESIGN.md
final class Grid<T> { Grid(int w, int h, Function<Position, ? extends T> initial);
                      int width(); int height(); boolean inBounds(Position); T get(Position); void set(Position, T); }
interface GameEvent {}
interface EventListener<E extends GameEvent> { void onEvent(E event); }
final class EventBus { <E extends GameEvent> void subscribe(Class<E>, EventListener<? super E>); void publish(GameEvent); }
record EnemyDefeated(Position position, int scoreValue, boolean boss) implements GameEvent {}
record PlayerDamaged(int playerIndex, int livesLeft) implements GameEvent {}
record HudMessage(String text) implements GameEvent {}
class GameException extends RuntimeException      // unchecked; InvalidMapException, ConfigLoadException, InvalidActionException

// model
abstract class Entity { long id(); Position position(); void moveTo(Position); boolean isActive(); void deactivate();
                        abstract EntityKind kind(); boolean blocksMovementOf(Entity mover) /* default false */; }
interface Damageable { boolean takeDamage(Damage damage); }   // true if the target was hurt
record Damage(int amount, long activationId, DamageSource source) { static long newActivationId(); }
enum DamageSource { EXPLOSION, BOSS_ATTACK, ENEMY_CONTACT, HAZARD }
final class Player extends Entity implements Damageable { Player(Position spawn, int playerIndex, double baseSpeed); ... }
final class Exit extends Entity { boolean isOpen(); void open(); }
final class GameWorld {
  GameWorld(String mapName, int width, int height, Player player, double timeLimit, Random random);
  void add(Entity); List<Entity> entities(); <T extends Entity> List<T> entities(Class<T>);
  List<Entity> entitiesAt(Position); <T extends Entity> Optional<T> firstAt(Position, Class<T>);
  boolean inBounds(Position); boolean isBlockedFor(Entity mover, Position target); void removeInactive();
  void setWarnings(Object source, Set<Position>); void clearWarnings(Object source); Set<Position> warningTiles();
  Player player(); Random random(); EventBus bus(); int score(); void addScore(int);
  double timeLeft(); void reduceTime(double); GameState state(); void setState(GameState);
}

// engine
interface GameSystem { void update(GameWorld world, double dt); }
final class SnapshotBuilder { static WorldSnapshot build(GameWorld); }

// input
enum Action { MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, PLACE_BOMB, SELECT_NORMAL, SELECT_PUMPED,
              SELECT_REMOTE, DETONATE, PAUSE, CONFIRM, BACK }
record Command(int playerIndex, Action action) {}
interface InputSource extends AutoCloseable { void start(Consumer<Command> sink); void close(); }

// view
record WorldSnapshot(int width, int height, List<Sprite> sprites, Set<Position> warnings, Hud hud, GameState state) {
  record Sprite(long id, EntityKind kind, Position position) {}
  record Hud(int lives, int score, int secondsLeft, BombType selectedBomb) {}
}
interface Renderer { void render(WorldSnapshot snapshot); }
```

Rules that come with them:

- **One entity list.** `GameWorld` keeps every entity in one list, and the F00.1 collections are typed queries: `world.entities(Bomb.class)`, `world.entities(Enemy.class)`, and so on. So `GameWorld` needs no import from another member's package. Queries return copies of the active entities, so a system may add entities while iterating.
- **Blocks, lava, and the exit are entities too.** Each subclass answers `blocksMovementOf(mover)` according to the "Who blocks whom" table, and `GameWorld.isBlockedFor` asks every entity on the target tile. A new obstacle needs no change in `PlayerSystem` or `EnemySystem`.
- **Warnings are owned by a source.** A boss attack or a Lava Tile calls `setWarnings(this, tiles)` and `clearWarnings(this)`. Renderers draw the union.
- **Randomness** comes only from `world.random()`, which tests seed.
- **Spec values** such as `Player.START_LIVES` are constants for now. `M-05` moves them into `config/game.properties`.
- **`TestWorlds`** knows `.`, `P`, `X`, and `#` (a test wall). Pass a legend for your own characters, for example `TestWorlds.fromAscii(Map.of('D', DestructibleBlock::new), rows)`. When a real block class lands, its owner replaces the test wall in `TestWorlds`.

How the contracts remove cross-member dependencies:

- **Damage** — `ExplosionSystem` (Marshall), `BossSystem` (Ilham), and `HazardSystem` (Areli) all call `Damageable.takeDamage(Damage)`. `Player` (Laura), `NormalEnemy`, and `Boss` (Ilham) implement it. Nobody needs the other side's code to test their own.
- **Drawing** — Every entity and tile reports an `EntityKind`. A new kind needs one enum constant and one entry in each renderer.
- **Warnings** — Boss attacks and lava add warning tiles to `GameWorld`. Renderers draw them without knowing where they came from.
- **Score and messages** — Systems publish events such as `EnemyDefeated`. `ScoreSystem` and the HUD subscribe.

### Time, threads, and the tick

- **Three named threads.** `input` blocks on key reads and puts `Command` objects on a `BlockingQueue<Command>`. `game-loop` is the only thread that changes the model. `render` draws the latest `WorldSnapshot`, which the loop publishes through a `volatile` reference. The threads are started through an `ExecutorService` and shut down together.
- **No shared mutable state between threads** other than the queue and the snapshot reference. There are no nested locks, so there is no deadlock risk. PRD open question 1 asks whether this satisfies the Concurrency requirement.
- **In the GUI, libGDX owns the window loop.** `Main` starts the `game-loop` thread and then the libGDX application. libGDX calls its `render()` method on its own thread every frame. That method draws the latest snapshot and takes the place of the `render` thread. No game rule runs inside it, and libGDX's frame time is never used as `dt`. Closing the window stops the `game-loop` thread. libGDX is imported only in `view/gui` and `app`.
- **Tick rate** — 20 ticks per second (`dt` = 0.05 s). Value in `config/game.properties`.
- **Fixed timestep with an accumulator.** The loop measures real elapsed time and runs one tick of `dt` for every `dt` that has passed, so 300 game seconds take 300 real seconds. After a stall it runs at most 5 catch-up ticks and drops the rest, so the game never fast-forwards past the player. `engine/GameLoop` is the only class allowed to read the clock.
- **All timing counts down by `dt`.** Bomb fuses, explosion duration, invulnerability, boss warning and cooldown, the lava cycle, and the game timer are plain numbers reduced each tick. No model or system class sleeps or reads the wall clock.
- **Pause** — When the state is not `PLAYING`, the loop skips the systems. Every timer freezes because nothing reduces it, which is exactly F09.5.
- **Restart** — The composition root builds a new `GameWorld` from the map file. Nothing is reset field by field.

As built in `C-02`:

- `engine/GameLoop` takes the world, the `BlockingQueue<Command>`, a list of `CommandHandler`s, the systems from `GameFactory.systems()`, the tick rate, the catch-up limit, and a clock (`System::nanoTime`, or a fake in tests). `snapshot()` returns the latest snapshot from any thread.
- `engine/RenderLoop` draws the latest snapshot on the `render` thread whenever it changes.
- `app/GameSession` runs the three tasks on one `ExecutorService`, renames each pool thread to `input`, `game-loop`, or `render`, and `close()` stops all three and waits up to 2 seconds.
- `view/cli/CliInput` polls the terminal every 100 ms, so `close()` takes effect without a key press.
- `input/KeyBindings` reads `config/keybindings.properties`: `p<player>.<ACTION> = key, key`. A key is one printable character, with letters case-insensitive, or one of `UP`, `DOWN`, `LEFT`, `RIGHT`, `ENTER`, `ESCAPE`, `SPACE`, `BACKSPACE`, `TAB`. A key bound twice, an unknown action or key, or a `p1` action with no key is a `ConfigLoadException`.

Order of work inside one tick:

1. Drain the command queue and pass each command to every `CommandHandler`. Handlers run in every state, so the pause command works while paused. `PlayerSystem` is a handler and a system, and it ignores moves unless the state is `PLAYING`.
2. `EnemySystem`, then `BossSystem`.
3. `BombSystem`, then `ExplosionSystem`.
4. `HazardSystem`.
5. `PowerUpSystem`.
6. Reduce the game timer.
7. `WinLoseSystem`.
8. Build and publish the `WorldSnapshot`.

`ScoreSystem` has no slot. It reacts to events.

### Entity lifecycle

- **Created** by `EntityFactory` when the map loads, or by a system during play: a bomb placed, an explosion started, a power-up dropped.
- **Removed** in one place. A system sets `active` to false. The loop removes inactive entities once, at the end of the tick. No system removes an entity from a collection while another system may be iterating it.
- **Reset** by building a new `GameWorld`, as described under Restart.

### Movement and input model

- **Tile steps.** Every entity occupies exactly one tile. A move is one step to an adjacent tile, followed by a cooldown of `1 / speed` seconds. Nothing moves more than one tile per tick, so nothing can skip over a tile. The snapshot carries each mover's previous tile and step progress, so the GUI can draw the step smoothly (`G-04`).
- **Actions, not keys.** `input/Action` names every action: the four moves, place bomb, select Normal, select Pumped, select Remote, detonate, pause, and the menu actions. `KeyBindings` maps keys to actions for each player from `config/keybindings.properties`. A key bound twice is a `ConfigLoadException`. Gameplay code and the How to Play screen read actions and bindings, never a key literal.
- **The terminal reports key presses only.** There is no key-release event, and when two keys are held only the last one repeats. The CLI input source therefore sends one `Command` per key event, and a held key moves through the operating system's key repeat. `S-02` confirms this on the team's terminals.
- **The GUI knows which keys are held**, because libGDX reports both key down and key up. Its input source sends a move `Command` every frame while a move key is down, and `PlayerSystem` keeps only the latest one. The engine treats both sources the same way.
- **One buffered move.** If a move arrives while the cooldown is running, `PlayerSystem` keeps the latest one for up to 0.15 seconds and applies it when the cooldown ends, so a slightly early key press is not lost.
- **Discrete actions fire once per press.** A repeat of the same discrete action within 0.2 seconds is ignored, so holding the pause key does not toggle pause repeatedly.

### Who blocks whom

One table for the collision rules, so four people implement the same behavior. Rows marked *assumed* are not stated in the specification. See PRD open question 11.

| On the tile | Player | Normal Enemy | Explosion arm | Boss line attack |
| --- | --- | --- | --- | --- |
| Map edge, Indestructible Block | Blocked | Blocked | Stops. Tile not hit. | Stops |
| Destructible Block | Blocked | Blocked | Stops. Block destroyed. Tile not hit. | Stops. Block unharmed. |
| Reinforced Block | Blocked | Blocked | Stops. Destroyed only by a Pumped Bomb. | Stops. Block unharmed. |
| Active bomb | Blocked, except when leaving the tile where it was placed | Blocked (*assumed*) | Passes. Bomb is triggered. | Passes (*assumed*) |
| Normal Enemy or boss | Blocked. Counts as contact. | Blocked (*assumed*) | Passes. Deals damage. | Not applicable |
| Player | Not applicable | Blocked. Counts as contact. | Passes. Deals damage. | Passes. Deals damage. |
| Lava Tile | Walkable | Walkable (*assumed*) | Passes | Passes |
| Power-up | Walkable. Collected. | Walkable. Not collected. | Passes. Unaffected. | Passes |
| Exit | Walkable | Walkable (*assumed*) | Passes | Passes |

**Contact.** A player and an enemy never share a tile, so contact means one of them tries to step onto the other's tile. `PlayerSystem` reports the player stepping into an enemy, and `EnemySystem` reports an enemy stepping into the player. Both call `takeDamage` on the player.

### Enemy AI

- **Decision is separate from motion.** A `MovementBehavior` returns the tile the enemy wants next. `EnemySystem` applies it through the blocking rules above.
- **Chase Enemy is a two-state machine**, `PATROL` and `CHASE`. Transition logic lives in the states, not in flags spread across the system.
- **Pathfinding is breadth-first search** on the 14 × 10 grid with four directions. The path is recomputed only when the player enters a new tile or the next step becomes blocked, not every tick.
- **Stuck handling.** If there is no path, or the next step stays blocked for 1 second, the enemy returns to its patrol path.
- **Detection** is a Manhattan distance of 3 tiles or less, without line of sight. This is assumed. See PRD open question 10.
- **State changes are logged** at DEBUG level with the enemy's identifier, for enemies and for the boss, so behavior can be tuned by reading the log.

### Screens

Screens form a stack in `app`: Menu, How to Play, Game, Pause, and Game Over. The top screen receives input and is drawn. Opening How to Play or Pause pushes a screen, and going back pops it. `GameState` stays the model's state from the specification. How to Play is a screen and has no `GameState` value.

### Map file

`config/maps/red-mountain.txt` holds 10 lines of 14 characters in the F07.2 legend. This is the layout from F07.3.3:

```text
##############
#P..D...H...X#
#.##.##.H##E.#
#...H...H....#
###.###.###..#
#DE.H...D....#
#.##.###.##H.#
#...HE...B...#
#E..D....H..E#
##############
```

It contains 1 player spawn, 5 Normal Enemy spawns, 1 boss spawn, 1 exit, 8 Lava Tiles, and 4 Destructible Blocks. Patrol paths, the split between Patrol and Chase enemies, and the guaranteed Remote Detonator are defined in `config/` by `M-05`.

### Technical requirement map

This table is the source for the demo explanation and the booklet. Update it when a class is renamed.

| Requirement | Where |
| --- | --- |
| Inheritance | `Entity` → `Player`, `Enemy` → `NormalEnemy` and `Boss` → `Hades`. `Bomb` → three bomb types. `Block` → three block types. `Tile` → `FloorTile`, `LavaTile`. |
| Abstract class or interface | Abstract `Entity`, `Bomb`, `Block`, `Boss`. Interfaces `GameSystem`, `Renderer`, `InputSource`, `AttackPattern`, `MovementBehavior`, `Damageable`, `EventListener`. |
| Polymorphism | The loop calls `update` on every `GameSystem`. `ExplosionSystem` calls `takeDamage` on any `Damageable`. `BossSystem` runs any `AttackPattern`. |
| Generics | `Grid<T>`, `EventListener<E extends GameEvent>`, `EventBus.subscribe(Class<E>, EventListener<? super E>)`, `GameWorld.entities(Class<T>)`. |
| Exceptions | `GameException` hierarchy. `MapValidator` throws `InvalidMapException` and the menu shows the message. |
| Collections | `List` for entities, `Map` and `EnumMap` for key bindings and glyphs, `Set<Position>` for explosion and warning tiles, `Deque` for breadth-first search, `BlockingQueue` for commands. |
| Concurrency | The three threads, `ExecutorService`, `BlockingQueue`, and the `volatile` snapshot. |

| Design pattern | Where | Why |
| --- | --- | --- |
| State | `GameState` and the game flow. `LavaState`. Chase Enemy `PATROL` and `CHASE`. Boss warning, active, and cooldown. | Behavior changes with the state and transitions are explicit. |
| Strategy | `MovementBehavior` for enemies. `AttackPattern` for Hades. | A new behavior or attack is a new class. |
| Factory Method | `EntityFactory` from map characters. `BombFactory` from `BombType`. | Creation rules sit in one place. |
| Observer | `EventBus` with `ScoreSystem`, the HUD, and the logger as listeners. | Systems stay unaware of each other. |
| Command | `Command` records from `InputSource`, queued for the game loop, applied by `PlayerSystem`. | CLI, GUI, and a second player share the same actions. Input is decoupled from when and where it is applied. |

MVC is the overall architecture and is described in the booklet beside these five.

| SOLID principle | Where |
| --- | --- |
| Single Responsibility | One `GameSystem` per concern. Loading, validating, and creating entities are three classes. |
| Open/Closed | A new bomb type, attack pattern, or tile is added without editing existing classes. |
| Liskov Substitution | Any `Bomb`, `Block`, or `Enemy` subtype works wherever the base type is expected. |
| Interface Segregation | `Damageable`, `Renderer`, and `InputSource` are small and separate. |
| Dependency Inversion | `engine` depends on `Renderer` and `InputSource`, not on JLine or libGDX. |

## Implementation Plan

The same IDs are used in `TODO.md`. Prefixes: `S` setup and deliverables, `C` core and MVP, `B` bomb and related, `E` enemy and boss, `M` map and environment, `G` GUI, `X` other bonuses.

After `C-01` on 12 November, the only tasks that depend on another member's work are `C-09`, `S-10`, and `X-04`.

### Setup and deliverables

| ID | Step | Owner | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- | --- |
| S-01 | Scaffold the Gradle project, `Makefile`, `.gitignore`, logging config, and the `src`, `config`, `doc` folders | Laura | Milestone 1 | — | 11 Nov | On a clean clone, `make build` and `make test` succeed and `make run` prints a placeholder. |
| S-02 | Terminal spike: raw key input and full-screen redraw with JLine 3 | Laura | Pedoman 3 | S-01 | 12 Nov | A demo echoes keys without Enter and redraws 20 times per second without flicker on all four members' terminals. The result records whether key release and two keys held together are reported. If the demo fails, switch to Lanterna and update this file. |
| S-03 | Choose the assistant, invite them to the repository, and book Asistensi 1 | Laura | Asistensi | — | 11 Nov | The assistant sheet is filled before 20.10 WIB, the assistant has repository access, and a session date is agreed at least 24 hours ahead. |
| S-04 | Hold Asistensi 1 and resolve the PRD open questions | Laura | Asistensi | S-03 | 16 Nov | Each open question has an answer in `PRD.md`, and every attending member submitted the form within 1 hour. |
| S-05 | Produce the two Milestone 1 PDFs: task division and class diagram | Laura | Milestone 1 | C-01 | 17 Nov | Both PDFs are in `doc/` and submitted with the repository link before 20.10 WIB. Each owner supplied the diagram for their packages. |
| S-06 | Hold Asistensi 2 | Laura | Asistensi | — | 3 Dec | Session held and every attending member's form submitted within 1 hour. |
| S-07 | Write the booklet: A5, at most 15 pages, PDF | Areli | Buklet | S-05 | 3 Dec | The PDF has the cover, user manual, roles, final class diagram, design patterns, and development story, and every member reviewed their section. |
| S-08 | Compile the activity log into one PDF | Areli | Log Activity | — | 4 Dec | `docs/log-activity.md` has entries for all four members and is exported to PDF. |
| S-09 | Publish the release and make the repository public | Laura | Milestone 2 | S-07, S-08, S-10 | 4 Dec | A GitHub release containing the source, booklet, and activity log exists before 18.00 WIB, the repository is public, and the form is submitted. |
| S-10 | Full integration test on `main` | All | Pedoman 10 | All `C`, `B`, `E`, `M` tasks | 2 Dec | Every acceptance criterion in `PRD.md` section 4 is checked by hand on the merged build, and each failure has a `TODO.md` row. |

### Core and MVP (Laura)

| ID | Step | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- |
| C-01 | Core types and contracts listed in the Contracts section, plus `TestWorlds.fromAscii` | F00.1 | S-01 | 12 Nov | Everything compiles, each contract has a unit test, and a sample `GameSystem` with a test runs without the game loop. Signatures are recorded in this file. |
| C-02 | Game loop and the three threads | F09.3 | C-01 | 13 Nov | 300 game seconds take 300 real seconds within 1 second. A 2-second stall causes at most 5 catch-up ticks. Commands arrive through the queue from keys read out of `config/keybindings.properties`, and a key bound twice is refused. Threads are named, and the program exits cleanly with no thread left running. |
| C-03 | CLI renderer and HUD | F09.3, F07.2 | C-01, S-02 | 13 Nov | The screen shows the map with the glyphs in `DESIGN.md` and a HUD with lives, score, timer, and selected bomb. Only changed cells are rewritten. A terminal too small for the map and HUD shows a message asking to enlarge it. |
| C-04 | Player movement and collision | F01.2.1, F01.2.3 | C-01 | 14 Nov | Tests show that movement follows the player's speed, is stopped by blocks, enemies, active bombs, and the map edge, keeps one early move for 0.15 seconds, and reports contact when the player steps into an enemy. |
| C-05 | MVP-0: a playable walking skeleton | F09 | C-02, C-03, C-04 | 14 Nov | From the menu, the Red Mountain map loads through `TestWorlds.fromAscii`, Deeper walks, the timer counts down, pause works, and the game ends in LOSE at 0 seconds. |
| C-06 | Lives, damage, invulnerability, and respawn | F01.2.2, F01.2.4 | C-04 | 19 Nov | Tests show one life lost per activation, 1.5 seconds of invulnerability, respawn at the spawn point or the nearest free tile, and LOSE at 0 lives. |
| C-07 | Menu, How to Play, pause menu, restart, and the game over screen | F09.1, F09.5, F00.4 | C-02 | 20 Nov | Screens are a stack, and back returns to the previous screen. Invalid menu input shows an error and asks again. Pause offers resume, restart, and menu, and holding the pause key toggles once. Restart restores every initial value. The final score is shown at WIN and LOSE. |
| C-08 | Win and lose check, with the exit locked until all enemies are defeated | F09.4 | C-01 | 16 Nov | Tests show WIN only when the 5 Normal Enemies and the boss are defeated and the player is on the exit, and LOSE at 0 lives or 0 seconds. |
| C-09 | MVP-1: integrate the first slice from each member | F00.2 | C-05, C-08, B-01, B-02, M-01, E-01 | 17 Nov | On `main`, the map loads from `config/maps/red-mountain.txt`, a Normal Bomb destroys a Destructible Block and a Patrol Enemy, and both WIN and LOSE can be reached. |

### Bomb, Explosion, Block, Power-Up, Score (Marshall)

| ID | Step | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- |
| B-01 | `Bomb` base, Normal Bomb, placement rules, and capacity | F02.1, F02.2.1, F02.3, F01.2.5 | C-01 | 15 Nov | Tests show a 3-second fuse, placement refused at full capacity or on an occupied tile, and capacity returned after the explosion ends. |
| B-02 | Explosion spread, duration, and damage | F03 | B-01, B-03 | 16 Nov | Tests show the center and four arms, a 0.5-second duration, arms stopping at each block type as in F03.2, power-ups unaffected, and `takeDamage` called once per target. |
| B-03 | The three block types | F04 | C-01 | 15 Nov | Tests show each type's movement and explosion rules and that a destroyed block's tile becomes walkable. |
| B-04 | Pumped Bomb and bomb type selection | F02.2.2, F02.3.2 | B-01, B-02, B-03 | 20 Nov | Tests show a 4.5-second fuse, range `blastRange + 2` capped at 7, Reinforced Blocks destroyed, 2 damage, and that selection does not change bombs already placed. |
| B-05 | Remote Bomb and detonate | F02.2.3, F02.3.3 | B-01 | 21 Nov | Tests show Remote Bomb refused before the Remote Detonator, no fuse, detonate exploding all of the player's Remote Bombs, and detonate failing safely with none active. |
| B-06 | Bounded chain reactions | F02.3.7, F03.2.6 | B-02 | 23 Nov | A test with every walkable tile holding a bomb finishes in one pass, and each bomb explodes exactly once. |
| B-07 | Power-ups: five types, drops, and the guaranteed Remote Detonator | F06 | B-03 | 25 Nov | Tests show the 30% drop with a seeded random source, each effect and its cap, pickup by the player only, and effects kept after losing a life. |
| B-08 | Score | F08 | C-01 | 26 Nov | Tests show 100 per Normal Enemy, 500 for Hades, and on WIN only 100 per life and 10 per remaining second. |

### Enemy and Boss (Ilham)

| ID | Step | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- |
| E-01 | `Enemy` base and Patrol Enemy | F05.1, F05.2 | C-01 | 15 Nov | Tests show an enemy following its configured path, turning at obstacles, and never entering a blocked tile. |
| E-02 | Chase Enemy | F05.2 | E-01 | 19 Nov | Tests show the switch to CHASE within 3 tiles, movement toward the player along a path around blocks, the path recomputed only when the player changes tile, the return to the patrol path beyond 3 tiles, and the return to patrol after 1 second with no path. |
| E-03 | Contact damage and enemy defeat | F01.2.2, F03.2.5 | E-01 | 20 Nov | Tests show an enemy stepping into the player costing one life without the two sharing a tile, an explosion defeating a Normal Enemy, and an `EnemyDefeated` event published once. |
| E-04 | Boss framework: health, phases, attack cycle, and vulnerability | F05.3.1 | C-01 | 22 Nov | Tests with a fake attack show warning 1 second, active, cooldown 5 seconds, vulnerability for the first 4 seconds after a designated attack only, phase 2 at half health, no repeated attack when another is usable, and 1 or 2 damage by bomb type. |
| E-05 | Hades phase 1: Heavy Punch, Spin Attack, Beam Attack, Ground Slam | F05.3.4 | E-04 | 24 Nov | For each attack, a test compares the affected tiles and duration with the PRD table for every facing direction. |
| E-06 | Hades phase 2 variants | F05.3.4 | E-05 | 26 Nov | For each attack, a test compares the phase 2 tiles with the PRD table. |
| E-07 | Attacks stopped by blocks, and warning tiles published for drawing | F05.3.1 | E-05 | 26 Nov | Tests show a straight attack stopping at a block, no block damaged, and the warning tiles present in `GameWorld` for exactly 1 second. |

### Map and environment (Areli)

| ID | Step | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- |
| M-01 | Map file format, `MapLoader`, and `EntityFactory` | F00.2, F07.1, F07.2 | C-01 | 15 Nov | `config/maps/red-mountain.txt` loads into a `GameWorld` with every entity at the position in the layout. An unreadable file or unknown character gives a clear error. |
| M-02 | `MapValidator` | F00.3 | M-01 | 19 Nov | One test per rule: size, the four counts, spawns on walkable tiles, no overlapping spawns, the player reaching the boss and the exit, and the map being completable. A failure names the rule. |
| M-03 | Tiles and the Lava Tile cycle | F07.3.3 | C-01 | 21 Nov | Tests show DORMANT 3 seconds, WARNING 1 second, ERUPTING, then DORMANT again, with the cycle frozen while paused. |
| M-04 | Lava damage and warning tiles | F07.3.3, F01.2.2 | M-03 | 23 Nov | Tests show at most one life lost per eruption, no damage while invulnerable, and warning tiles published during WARNING. |
| M-05 | Configuration: `game.properties`, patrol paths, the Patrol and Chase split, and the guaranteed Remote Detonator | F05.2, F06.3 | M-01 | 24 Nov | Every tunable value is read from `config/`, an invalid value gives a `ConfigLoadException` with the key name, and a test shows the Remote Detonator is always obtainable. |
| M-06 | How to Play text and the README feature table | F09.1.1 | — | 26 Nov | The How to Play screen lists the goal and every control in the key bindings, and `README.md` shows the current status of every feature. |

### GUI, bonus B01 (Laura)

| ID | Step | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- |
| G-01 | libGDX window and `GuiRenderer` | B01 | C-05 | 22 Nov | `make run-gui` opens a window on all four members' machines and draws the same `WorldSnapshot` as the CLI with the colors in `DESIGN.md`. Resizing the window scales the map and keeps all of it visible. Closing the window stops every thread. The libGDX version is recorded in the stack table. The CLI still builds and runs. |
| G-02 | GUI key input | B01 | G-01 | 24 Nov | Every action available in the CLI works in the GUI through the same `Command` objects, and two keys held together are both read. |
| G-03 | GUI menu, HUD, pause, and game over screens | B01 | G-01, C-07 | 27 Nov | The full flow from menu to WIN or LOSE can be played without the terminal. Menus and the HUD are laid out with Scene2D tables, not fixed positions. |
| G-04 | Sprites and animation for movement, explosions, warnings, and lava states | B01 | G-01 | 1 Dec | A step between tiles is drawn smoothly. Explosion, warning, and the three lava states are each distinguishable at a glance. The game holds 20 ticks per second. |

### Other bonuses

| ID | Step | Owner | Spec | Depends on | Due | Completion criteria |
| --- | --- | --- | --- | --- | --- | --- |
| X-01 | Document five design patterns with class names and reasons | Laura | B05 | — | 1 Dec | The pattern table in this file matches the code, and the booklet has the same table. |
| X-02 | Bomb Kick | Marshall | B02 | B-01 | 29 Nov | Tests show a kicked bomb sliding until it meets an obstacle, the map edge, or an entity, with its fuse still running. |
| X-03 | Map Maker with validation, save, and load | Areli | B03 | M-02 | 1 Dec | A map made in the editor is saved to `config/maps/`, passes `MapValidator`, and can be loaded and played. An invalid map is refused with the rule named. |
| X-04 | Local Multiplayer, two-player co-op | Marshall | B04 | C-06, B-07 | 1 Dec | Two players with separate key bindings share the map, score, and timer. The eight rules in B04 each have a test or a recorded manual check. Both players moving at the same time is verified in the GUI. The terminal's limit is stated in How to Play. See PRD open question 12. |
| X-05 | Creativity feature | Ilham | BXX | — | 1 Dec | **Pending.** The team picks the feature by 20 Nov and replaces this row with real criteria. |
