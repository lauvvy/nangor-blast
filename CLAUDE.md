# CLAUDE.md — Nangor Blast

Nangor Blast is a real-time Bomberman game for the command line, written in Java for the IF2010 Object-Oriented Programming course project. One player controls Deeper on a 14 × 10 map, defeats five Normal Enemies and the boss Hades, and reaches the exit before a 300-second timer runs out. The team builds map type C, Red Mountain.

Read these before working:

- [`PRD.md`](docs/PRD.md) — scope and acceptance criteria.
- [`ARCHITECTURE.md`](docs/ARCHITECTURE.md) — structure, contracts, and the implementation plan.
- [`TODO.md`](docs/TODO.md) — current task status and the branch for each task.
- [`WORKFLOW.md`](docs/WORKFLOW.md) — when to proceed, when to ask, what done means, and the branch plan.
- [`IF2010 Spesifikasi Tugas Besar.md`](docs/IF2010%20Spesifikasi%20Tugas%20Besar.md) — the course specification, in Indonesian. It overrides every other document.

This file is at the repository root. Every other project document is in `docs/`.

## Tech Stack

No code exists yet. The scaffold is task `S-01`.

| Tool | Version or role |
| --- | --- |
| Java | 21 LTS (Temurin 21.0.13 installed) |
| Gradle | Wrapper (`gradlew`), created in `S-01`. Gradle itself is not installed. |
| Make | `Makefile` that calls `gradlew`. Required by the specification. Not installed on the lead's machine yet. |
| JLine 3 | Raw keyboard input and terminal drawing. Unverified until spike `S-02`. Fallback: Lanterna. |
| libGDX with LWJGL3 | GUI for bonus B01. Version pinned in `G-01`. Used only in `view/gui` and `app`. |
| SLF4J with Logback | Logging to `logs/nangor-blast.log` |
| JUnit 5 | Unit tests |

## Design System

[`DESIGN.md`](docs/DESIGN.md) defines the terminal glyphs, the colors, the draw priority, the screen layout, and the scaling rules for both views.

- `core/EntityKind` lists everything that can be drawn.
- `view/cli` maps each `EntityKind` to a glyph. `view/gui` maps it to a color or sprite.
- When a new `EntityKind` is added, update `DESIGN.md`, the CLI mapping, and the GUI mapping together.

## Project Structure

Planned layout. Imports go one way: `input` → `engine` → `model`, and `engine` → `view` through a snapshot.

```text
src/main/java/nangorblast/
  app/              Main and GameFactory, the composition root          Laura
  core/             Position, Direction, GameState, Grid<T>, events     Laura
  model/            GameWorld, Entity, Damageable, Player               Laura
  model/bomb/       Bomb types and BombFactory                          Marshall
  model/explosion/  Explosion                                           Marshall
  model/block/      Block types                                         Marshall
  model/powerup/    PowerUp types                                       Marshall
  model/enemy/      Normal Enemy and movement behaviors                 Ilham
  model/boss/       Boss, Hades, attack patterns                        Ilham
  model/map/        GameMap, Tile, LavaTile                             Areli
  mapio/            MapLoader, MapValidator, EntityFactory, GameConfig  Areli
  editor/           Map Maker, bonus B03                                Areli
  engine/           GameLoop, GameSystem, PlayerSystem, WinLoseSystem   Laura
  engine/bomb/      Bomb, explosion, power-up, and score systems        Marshall
  engine/enemy/     Enemy and boss systems                              Ilham
  engine/hazard/    Lava system                                         Areli
  input/            Command, InputSource, KeyBindings                   Laura
  view/             Renderer, WorldSnapshot                             Laura
  view/cli/         Terminal renderer and input                         Laura
  view/gui/         libGDX renderer and input, bonus B01                Laura
src/test/java/nangorblast/   Tests, mirroring the packages above
assets/             Sprites and the font for the GUI                    Laura
config/             maps/red-mountain.txt and game.properties           Areli
                    keybindings.properties                              Laura
docs/               Project documents, specification, activity log
doc/                Submission PDFs, required by the specification. Created in S-01.
.claude/skills/     Project skills for Claude Code. Not committed.
```

This agent works for Laura. Ask before editing a package owned by another member.

## Code Conventions

- Keep the layers separate. `model` has no I/O, no threads, no clock, and no import from `engine`, `input`, or `view`. `view` never changes the model. `input` only creates `Command` objects.
- Count time down with `dt`. Fuses, cooldowns, invulnerability, the lava cycle, and the game timer are numbers reduced each tick. This is what makes pause work.
- Only the `game-loop` thread changes game state. Other threads use the command queue or the published snapshot.
- Put one concern in one `GameSystem`. Register it with one line in `app/GameFactory`.
- Read named actions, never keys. Keys are bound to `input/Action` values in `config/keybindings.properties`.
- Move entities one tile per step. Follow the "Who blocks whom" table in `ARCHITECTURE.md` for every collision rule.
- Remove an entity by setting `active` to false. The loop removes inactive entities at the end of the tick.
- Read tunable values from `config/game.properties`. Use the specification's numbers as the defaults.
- Validate every external input: keys, menu choices, map files, and configuration values. Throw a `GameException` subclass with a message that names the problem.
- Log with SLF4J. Give threads names.
- Write a unit test for each rule in a task's completion criteria. Build small worlds with `TestWorlds.fromAscii`.
- Use the specification's names for game concepts: `Player`, `Bomb`, `Explosion`, `Block`, `Enemy`, `PowerUp`, `GameState`, `blastRange`, `bombCapacity`.
- Write code, comments, and commit messages in English.
- If an instruction is ambiguous, ask what it means before coding.

## Features

Mandatory features. Check a box when the feature's tasks are **Done** in `TODO.md`.

- [ ] F00 Game initialization, map validation, restart
- [ ] F01 Player: movement, lives, respawn, bomb interaction
- [ ] F02 Bomb: Normal, Pumped, Remote
- [ ] F03 Explosion
- [ ] F04 Block: Indestructible, Destructible, Reinforced
- [ ] F05 Enemy: Patrol, Chase, boss Hades
- [ ] F06 Power-Up: Bomb Up, Fire Up, Speed Up, Remote Detonator, Heart
- [ ] F07 Map: Red Mountain with Lava Tiles
- [ ] F08 Score
- [ ] F09 Game flow: menu, states, pause, game over

Bonus features.

- [ ] B01 GUI
- [ ] B02 Bomb Kick
- [ ] B03 Map Maker
- [ ] B04 Local Multiplayer
- [ ] B05 Five design patterns
- [ ] BXX Creativity feature, not yet chosen

## Do Not

- Print to standard output during play. Use the logger, which writes to a file.
- Call `Thread.sleep`, `System.currentTimeMillis`, or `java.util.Timer` in `model` or in a `GameSystem`. Use `dt`. Only `engine/GameLoop` reads the clock.
- Rely on key-release events or on two keys held together in the terminal. The terminal reports key presses only.
- Recompute an enemy path every tick. Recompute when the player changes tile or the next step is blocked.
- Remove an entity from a collection inside a system.
- Add a lock or `synchronized` block without recording the reason in `ARCHITECTURE.md`. The single-writer loop is the concurrency design.
- Put game rules in a renderer, or drawing code in the model.
- Import libGDX outside `view/gui` and `app`, or run a game rule inside libGDX's `render()`. The game loop stays the engine's own thread, and libGDX's frame time is never `dt`.
- Hard-code a value the specification might change. Put it in `config/game.properties`.
- Add a dependency without the team's agreement. Record it in the stack table here and in `ARCHITECTURE.md`.
- Implement features of other map types: Water Tile, Switch, Gate, Ice Tile, Laser Trap, or other bosses.
- Change the specification file.
- Start bonus work for an area while its mandatory tasks are open, except GUI work, which starts once MVP-0 runs.

## Game-Development Skills

The game logic uses no engine. It is plain Java. JLine draws the terminal view and libGDX draws the GUI view. The installed game-development skills show their examples in Godot or Unity, and none of them covers libGDX. Use the idea and write it in Java against the contracts in `ARCHITECTURE.md`. Do not port an engine API. For a libGDX class or method, check the libGDX documentation instead of guessing.

Load the skill that matches the task before starting it.

| Task | Skill |
| --- | --- |
| `C-02` game loop | `physics-tuning`, for the fixed timestep |
| `C-04`, `G-02`, `X-04` input and key bindings | `input-systems` |
| `C-03`, `C-07`, `G-03` HUD, menus, screens | `game-ui-ux` |
| `E-01` to `E-07` enemies and boss | `game-ai` |
| `G-01`, `G-04` sprites and animation | `create-game-assets`, `game-feel` |
| `X-03` Map Maker save and load | `save-systems` |
| Changing the map layout, if the assistant allows it | `level-design` |

## Testing

Commands are available after `S-01`. Until then there is nothing to run.

```bash
./gradlew test
```

All tests pass, with zero failures. For anything that draws or reads keys, also run the game and check the behavior by hand, then record the result in `TODO.md`.

## Build

```bash
make build      # compile and package
make run        # start the CLI game
make run-gui    # start the GUI, bonus B01
make test       # run the unit tests
make clean      # remove build output
```

A successful build compiles with zero errors and zero warnings from `javac -Xlint:all`. The exact targets are confirmed in `S-01`.

## Git Rules

After finishing a change, summarize the result and provide Git commands in the response. The user runs `git add`, `git commit`, and `git push` themselves. Claude Code must leave staging, commits, and pushes to the user, including through other tools.

Work on the branch listed for the task in `docs/TODO.md`, for example `feat/C-01-core-contracts`. The branch plan and merge order are in `docs/WORKFLOW.md`.

Provide the commands in this order, with values that match the task.

1. `git add -- <changed-file-path>` to stage files related to the task.
2. `git diff --cached` to review the changes that will be committed.
3. `git commit -m "<commit-message>"` with a message describing the change.
4. `git push origin <branch>` to the remote `https://github.com/lauvvy/nangor-blast`.
