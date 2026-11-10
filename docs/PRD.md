# PRD — Nangor Blast

> **Status** Draft, pending Asistensi 1
> **Author** Laura Paula Gosal (18225060), with Claude Code
> **Last updated** 10 November 2026
> **Source** [`IF2010 Spesifikasi Tugas Besar.md`](IF2010%20Spesifikasi%20Tugas%20Besar.md), version 1.0

This document condenses the course specification into the scope the team builds. When this file and the specification disagree, the specification wins and this file is corrected.

## 1. Problem Statement

IF2010 Object-Oriented Programming requires each group to build **Nangor Blast**, a real-time Bomberman game for the command line in Java. The game has to show seven OOP concepts, at least two design patterns, and at least three SOLID principles, and the team has to explain each of them during assistance sessions and the demo.

Four people build the game in 24 days, with a first submission after 7 days. Without a shared scope, a stable set of interfaces, and one tracker, members block each other, features interact in untested ways, and the mandatory features are not finished before the bonus work starts.

## 2. Goals

Deliver a complete, demonstrable Nangor Blast for map type C (Red Mountain) and its boss Hades.

| Goal | Measure |
| --- | --- |
| Milestone 1 submitted | Repository link and two PDFs (task division, class diagram) submitted before 17 November 2026, 20.10 WIB. |
| Mandatory features complete | Every acceptance criterion in section 4 passes in the CLI build by 26 November 2026. |
| Technical requirements met | All seven OOP concepts, at least 2 design patterns (target 5), and at least 3 SOLID principles are each traceable to named classes in `ARCHITECTURE.md`. |
| Bonus features delivered | B01 to B05 and one creativity feature are finished by 1 December 2026, in the cut order in section 7. |
| Milestone 2 submitted | GitHub release with source code, booklet, and activity log published before 4 December 2026, 20.10 WIB, with the repository public. |
| Assistance completed | Asistensi 1 held by 16 November 2026 and Asistensi 2 by 3 December 2026, with every attending member's form submitted within 1 hour. |

## 3. Target Users

- **Player** — One person who controls Deeper from the keyboard. During the demo this is a team member or a course assistant.
- **Course assistants** — They grade the game, the booklet, and the team's explanation of the design. They also answer specification questions.
- **Team** — Four developers. Ownership is by package, so each member can work without waiting for another.

  | Member | NIM | Area |
  | --- | --- | --- |
  | Laura Paula Gosal | 18225060 | Core contracts, game loop, input, CLI, game flow, MVP, integration, GUI |
  | Marshall Farrell Attalla K | 18225062 | Bomb, Explosion, Block, Power-Up, Score, Bomb Kick, Local Multiplayer |
  | Muhammad Ilham Malik Amrullah | 18225022 | Normal Enemy, Boss framework, Hades, creativity feature |
  | Areli Matthew Widodo | 18225102 | Map loading and validation, Lava Tile, configuration, Map Maker, booklet, activity log |

- **AI coding agent** — Claude Code, working from `CLAUDE.md`, `WORKFLOW.md`, and `TODO.md`.

## 4. User Stories

Each story lists the specification section it comes from.

- As a player, I want a main menu so that I can start, read the instructions, or quit. (F09.1)
  - **Acceptance criteria** — The menu appears on launch with Start Game, How to Play, and Exit. An invalid choice shows an error and asks again. How to Play returns to the menu.
- As a player, I want the game to refuse a broken map so that I never start an unwinnable game. (F00.3)
  - **Acceptance criteria** — A map that breaks any validation rule prints a message naming the rule and the game does not enter PLAYING.
- As a player, I want to move Deeper in four directions in real time so that I can explore the map. (F01.2.1)
  - **Acceptance criteria** — A movement key moves Deeper one tile at the player's speed. Blocks, enemies, active bombs, and the map edge stop the move and leave the position unchanged.
- As a player, I want to place bombs so that I can destroy blocks and enemies. (F02)
  - **Acceptance criteria** — The bomb key places the selected bomb type on Deeper's tile and uses one capacity. It fails when capacity is used up or the tile already holds a bomb. Capacity returns when the bomb has finished exploding.
- As a player, I want to choose between Normal and Pumped Bomb so that I can trade fuse time for range and damage. (F02.2, F02.3.2)
  - **Acceptance criteria** — The HUD shows the selected type. A Normal Bomb explodes after 3 seconds. A Pumped Bomb explodes after 4.5 seconds with range `blastRange + 2`, capped at 7.
- As a player, I want to detonate Remote Bombs myself so that I can time an explosion. (F02.2.3, F02.3.3)
  - **Acceptance criteria** — Remote Bomb is unavailable until the Remote Detonator is collected. The detonate key explodes every active Remote Bomb the player owns. Remote Bombs never explode from a fuse.
- As a player, I want explosions to follow clear rules so that I can plan around them. (F03)
  - **Acceptance criteria** — An explosion lasts 0.5 seconds, covers the center and four arms, stops at blocks as described in section 5, triggers other bombs in range, and damages the player who placed it.
- As a player, I want to lose a life and respawn rather than lose at once so that a mistake is recoverable. (F01.2.2, F01.2.4)
  - **Acceptance criteria** — Any damage source removes exactly one life per activation. Deeper respawns at the spawn point, or the nearest free walkable tile, and is invulnerable for 1.5 seconds. At 0 lives the state becomes LOSE.
- As a player, I want Normal Enemies that patrol or chase so that the map is dangerous. (F05.2)
  - **Acceptance criteria** — The map has exactly 5 Normal Enemies with 1 health. A Chase Enemy switches to CHASE when Deeper is within 3 tiles and returns to its patrol path when Deeper leaves that range. Contact costs Deeper one life.
- As a player, I want a boss with readable attacks so that the fight is hard but fair. (F05.3)
  - **Acceptance criteria** — Every Hades attack shows a warning on the affected tiles for 1 second, then is active for its duration, then Hades cools down for 5 seconds. Hades takes damage only while vulnerable.
- As a player, I want power-ups so that Deeper gets stronger during the run. (F06)
  - **Acceptance criteria** — Walking onto a power-up applies its effect and removes it. Effects survive losing a life and reset on restart. Enemies cannot collect power-ups.
- As a player, I want lava that warns before it erupts so that I can cross it safely. (F07.3.3)
  - **Acceptance criteria** — Each Lava Tile repeats DORMANT for 3 seconds, WARNING for 1 second, then ERUPTING. Deeper loses at most one life per eruption.
- As a player, I want to see my lives, score, timer, and selected bomb so that I know my situation. (F08, F09.3)
  - **Acceptance criteria** — The HUD shows all four values and updates within one frame of a change.
- As a player, I want to pause so that I can stop without losing progress. (F09.5)
  - **Acceptance criteria** — While PAUSED, enemies, bomb fuses, lava, boss timers, and the game timer do not advance. The player can resume, restart, or return to the menu. Resume continues from the exact state before the pause.
- As a player, I want a clear end to the game so that I know whether I won. (F09.4)
  - **Acceptance criteria** — WIN requires all 5 Normal Enemies and Hades defeated and Deeper on the exit. LOSE happens at 0 lives or when the timer reaches 0. The final score is shown in both cases.
- As a course assistant, I want to build and run the game from the README so that I can grade it. (Ketentuan Umum)
  - **Acceptance criteria** — On a clean clone, the commands in `README.md` build and start the game without extra steps.

## 5. Functional Requirements

Requirement IDs are the specification section numbers.

### F00 — Game initialization

- **F00.1** The game holds the current map, the player, and collections of enemies, bombs, explosions, blocks, and power-ups, plus score, timer, and game state.
- **F00.2** Initialization loads the Red Mountain map, creates entities from it, starts with no bombs or explosions, sets score to 0 and timer to 300 seconds, starts the lava cycle, and enters PLAYING.
- **F00.3** A valid map is 14 × 10 tiles with exactly one player spawn, five Normal Enemy spawns, one boss spawn, and one exit. Spawns are on walkable tiles and do not overlap. The player can reach the boss area and the exit, and the map can be completed with blocks and lava taken into account. An invalid map shows an error and the game does not start.
- **F00.4** Restart returns every entity, the lava cycle, lives, bomb capacity, blast range, score, and timer to their initial values.

### F01 — Player

- **F01.1** Initial values: 3 lives, bomb capacity 1, blast range 1. Maximums: 5 lives, bomb capacity 3, blast range 5.
- **F01.2.1** Movement is real-time in four directions at the player's speed and cannot pass a block, an enemy, an active bomb, or the map edge.
- **F01.2.2** Explosions, boss attacks, contact with a Normal Enemy, and lava each remove one life. One activation damages the player once. An invulnerable player takes no damage.
- **F01.2.4** After losing a life with lives remaining, the player respawns at the spawn point with 1.5 seconds of invulnerability. If the spawn point is occupied, the nearest free walkable tile is used.
- **F01.2.5** The player can leave a tile holding their own bomb but cannot re-enter a tile with an active bomb.

### F02 — Bomb

- **F02.2.1 Normal Bomb** Default type. Fuse 3 seconds. Range equals the player's `blastRange`.
- **F02.2.2 Pumped Bomb** Available from the start. Fuse 4.5 seconds. Range is `blastRange + 2`, capped at 7. Destroys Reinforced Blocks. Deals 2 damage to a vulnerable boss.
- **F02.2.3 Remote Bomb** Requires the Remote Detonator. No fuse. The DETONATE command explodes all of the player's active Remote Bombs. Range equals `blastRange`. Deals 1 damage to a vulnerable boss.
- **F02.3** The selected type applies to the next placements until changed or until restart, and does not change bombs already placed. A bomb needs a walkable tile with no other active bomb. A bomb hit by another explosion explodes at once. One activation produces one explosion. Every active bomb uses one capacity.

### F03 — Explosion

- **F03.2.1** Duration 0.5 seconds. Shape is the center tile plus four straight arms.
- **F03.2.2, .7** An Indestructible Block stops the arm and is not part of the explosion area.
- **F03.2.8, .9** A Destructible Block in range is destroyed. The arm stops there, the block's tile is not part of the explosion area, and the tile becomes walkable.
- **F03.2.10, .11** A Reinforced Block stops every arm. Only a Pumped Bomb destroys it.
- **F03.2.3, .4, .5** An explosion hits the player, Normal Enemies, the boss, and active bombs in its area, and does not affect power-ups. The owner is not immune. Normal Enemies always take damage. The boss takes damage only while vulnerable.
- **F03.2.6** Chain reactions trigger other bombs in range and are bounded so the game cannot stall.

### F04 — Block

- **F04.2.1 Indestructible** Blocks movement and explosions. Cannot hold a bomb.
- **F04.2.2 Destructible** Blocks movement until destroyed. May release a power-up when destroyed.
- **F04.2.3 Reinforced** Blocks movement until destroyed by a Pumped Bomb. The Red Mountain layout contains none. It is implemented because F02.2.2 and F03 define its behavior and the Map Maker can place it. See open question 8.

### F05 — Enemy

- **F05.2 Normal Enemy** Exactly 5, each with 1 health. Each is a Patrol Enemy, which always follows its path, or a Chase Enemy, which has PATROL and CHASE states with a detection range of 3 tiles. The split between the two kinds is the team's choice.
- **F05.3.1 Boss rules** One tile in size. 8 health. Phase 2 starts at half health. Attack cycle: choose a pattern at random from those usable in the current phase and range, never the same one twice in a row when another is usable. Warning for 1 second with direction and target fixed at warning start. Active for the pattern's duration. Cooldown for 5 seconds. The boss is vulnerable only during the first 4 seconds of the cooldown that follows a designated attack. Boss attacks do not damage blocks, and straight attacks stop at a block. Contact with the boss costs one life.
- **F05.3.4 Hades** Vulnerable after Heavy Punch and Spin Attack.

  | Attack | Area | Duration | Phase 2 |
  | --- | --- | --- | --- |
  | Heavy Punch | 3 tiles straight ahead | 1 s | Two more tiles beside the end tile, forming a T. |
  | Spin Attack | 8 tiles around Hades | 1 s | Radius 2 around Hades. |
  | Beam Attack | The whole row or column through the player's position at warning start | 1 s | Two adjacent rows or columns. |
  | Ground Slam | 3 × 3 tiles in front of Hades | 1.5 s | 3 × 4 tiles. |

### F06 — Power-Up

- **F06.2** Bomb Up adds 1 bomb capacity up to 3. Fire Up adds 1 blast range up to 5. Speed Up adds 50% of the base speed, up to 100% in total. Remote Detonator unlocks Remote Bomb. Heart adds 1 life up to 5.
- **F06.3** Each destroyed Destructible Block has a 30% chance to release exactly one power-up, chosen at random from all types. The map guarantees at least one obtainable Remote Detonator. Effects last until the game ends or restarts.

### F07 — Map

- **F07.1** One map, Red Mountain, 14 × 10 tiles, using the given layout and the legend in F07.2.
- **F07.3.3 Lava Tile** Walkable. Cycle: DORMANT 3 seconds, WARNING 1 second, ERUPTING, then back to DORMANT. A player on the tile while it erupts loses at most one life.

### F08 — Score

- Normal Enemy defeated: 100. Hades defeated: 500. On WIN only: 100 per remaining life and 10 per remaining second.
- The score is shown during play, the final score is shown at WIN and LOSE, and restart sets it to 0.

### F09 — Game flow

- **F09.1** Main menu with Start Game, How to Play, and Exit, with input validation.
- **F09.2** States: MENU, PLAYING, PAUSED, WIN, LOSE.
- **F09.3** While PLAYING, user input, player actions, the enemy loop, the bomb and map loop, and rendering repeat, and the timer counts down.
- **F09.4** WIN and LOSE are checked after every cycle.
- **F09.5** Pause freezes every game process. The player can resume, restart, or return to the menu.

## 6. Non-Functional Requirements

- **Language and build** — Java 21. Gradle wrapper for build and packaging. A `Makefile` with build, run, test, and clean targets.
- **Repository layout** — `src`, `config`, `doc`, `Makefile`, and `README.md` exist at the repository root.
- **OOP concepts** — Inheritance, abstract class or interface, polymorphism, generics, exceptions, collections, and concurrency are each used and listed with their classes in `ARCHITECTURE.md`.
- **Design** — At least 2 design patterns and 3 SOLID principles for the mandatory work, 5 patterns for bonus B05.
- **Input validation** — Every menu choice, key, and configuration file is validated. Invalid input never crashes the program.
- **Concurrency safety** — No deadlock and no data race. Game state has one writer thread. Threads are named.
- **Responsiveness** — The game loop runs at 20 ticks per second. A key press is visible on screen within 100 ms. The tick rate is the team's choice and may change after the terminal spike (`S-02`).
- **Timing accuracy** — Game time follows real time: 300 game seconds take 300 real seconds within 1 second. After a stall the game runs at most 5 catch-up ticks.
- **Input** — Keys are bound to named actions in a configuration file. No gameplay code names a key.
- **Observability** — Structured logging through SLF4J to a file. Nothing is logged to standard output during play.
- **Testability** — Game rules are in classes with no terminal, window, or thread dependency, so they can be unit tested.
- **Portability** — The CLI runs in the terminals the four members use. This is unverified until `S-02`.

## 7. Scope

- **In scope** — F00 to F09 for map type C (Red Mountain) and boss Hades, in the CLI. Bonuses B01 GUI, B02 Bomb Kick, B03 Map Maker with save and load, B04 Local Multiplayer, B05 five design patterns, and one creativity feature.
- **Bonus cut order** — If time runs short, bonuses are dropped from the end of this list: B05, B02, B01, B04, B03, BXX. Mandatory features always come first. Any bonus unfinished on 1 December 2026 is dropped.
- **Out of scope** — The other four maps and their bosses. Water Tile, Switch, Gate, Ice Tile, and Laser Trap. Hatchlings. Online multiplayer. Saving and resuming a game in progress. Sound.
- **Changes** — Record agreed changes to scope here, with the date and the reason.
  - 10 November 2026 — Initial scope set from the specification v1.0 and the team lead's decisions (map C, all bonuses).

## 8. Open Questions

These are not answered by the specification. They go to the assistant at Asistensi 1 (`S-04`). Until then the team builds on the working assumption in the last column.

| # | Question | Working assumption |
| --- | --- | --- |
| 1 | Do an input thread, one game-loop thread, and a render thread satisfy the Concurrency requirement, or must the Enemy Loop and the Bomb & Map Loop in F09.3 be separate threads? | Three threads are enough. |
| 2 | How long does ERUPTING last? Do all Lava Tiles erupt together? Does lava damage enemies? | 1 second, all together, player only. |
| 3 | What are the base speeds of the player and the enemies? | Player 4 tiles per second, Normal Enemy 2, set in `config/`. |
| 4 | Where is the boss area in the Red Mountain layout, and does Hades move between attacks? | The open region around the boss spawn. Hades turns to face the player and does not walk. |
| 5 | The layout must follow the specification and also be "developed and modified". How much may change? The given layout has only 4 Destructible Blocks. | Keep the given layout. Add Destructible Blocks and one fixed Remote Detonator only if allowed. |
| 6 | May the Makefile only call Gradle? | Yes. |
| 7 | If the GUI is built, must the CLI stay fully playable? | Yes. Both run from the same game logic. |
| 8 | Must a group without Green Garden implement the Reinforced Block? | Yes. |
| 9 | How are bomb types selected: one key that cycles, or one key per type? | One key per type, including Normal Bomb. |
| 10 | How is the Chase Enemy's 3-tile detection range measured, and does a block between them hide the player? | Manhattan distance of 3 or less. Blocks do not hide the player. |
| 11 | Do active bombs and other enemies block a Normal Enemy? Can an enemy walk on lava and on the exit? | Bombs and enemies block. Lava and the exit are walkable for enemies. |
| 12 | A terminal reports key presses only, so two players cannot hold keys at the same time in the CLI. Is Local Multiplayer acceptable with full simultaneous input in the GUI only? | Yes. The CLI supports two players by tapping keys. |
