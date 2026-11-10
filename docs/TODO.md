# Project Tasks — Nangor Blast

Local tracker for the four-person team. IDs, owners, and due dates come from the Implementation Plan in [`ARCHITECTURE.md`](ARCHITECTURE.md), where each task's completion criteria are written.

**Last updated** 10 November 2026

## How to use this file

- **Status** is one of **Pending**, **In progress**, **Blocked**, or **Done**.
- Set a task to **In progress** when you start it, on the branch in the **Branch** column. The merge order is in the Branch plan in `WORKFLOW.md`.
- Set it to **Done** only after its completion criteria in `ARCHITECTURE.md` have been checked. Write what you checked in the last column.
- For **Blocked**, write what has to happen before work can resume and who can unblock it.
- Change only your own rows. If a due date has to move, tell the team and update `ARCHITECTURE.md` in the same commit.
- Add a row to `docs/log-activity.md` whenever you update a row here.

## Dates that cannot move

| Date (WIB) | Event |
| --- | --- |
| 11 Nov 2026, 20.10 | Assistant selection closes |
| 16 Nov 2026, 20.10 | Asistensi 1 deadline. Contact the assistant at least 24 hours before the session. |
| 17 Nov 2026, 20.10 | **Milestone 1** |
| 26 Nov 2026 | Team target: all mandatory features complete in the CLI |
| 1 Dec 2026 | Team target: bonus cut-off. Unfinished bonuses are dropped. |
| 3 Dec 2026, 20.10 | Asistensi 2 deadline |
| 4 Dec 2026, 20.10 | **Milestone 2** |

## Setup and deliverables

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| S-01 | Scaffold Gradle, Makefile, folders | Laura | `feat/S-01-scaffold` | Milestone 1 | Done | 11 Nov | 10 Nov: on Windows with `mingw32-make`, `make build`, `make test` (1 test), `make run` (prints placeholder), and `make clean` pass with `-Xlint:all -Werror`. Logs go to `logs/nangor-blast.log`. `src`, `config`, `doc`, and the root `README.md` exist. Not yet checked on macOS or Linux. |
| S-02 | Terminal spike with JLine 3 | Laura | `feat/S-02-terminal-spike` | Pedoman 3 | In progress | 12 Nov | 10 Nov: `make spike` builds with no warnings, and a piped run reads 5 keys and exits cleanly. Waiting for all four members to run it in a real terminal and fill the results table in `ARCHITECTURE.md`. |
| S-03 | Choose assistant, invite to repo, book Asistensi 1 | Laura | No branch | Asistensi | Pending | 11 Nov | Fill the assistant sheet before 20.10 WIB. |
| S-04 | Hold Asistensi 1 | Laura | No branch | Asistensi | Pending | 16 Nov | Bring the twelve open questions in `PRD.md` section 8. |
| S-05 | Milestone 1 PDFs | Laura | `docs/S-05-milestone-1` | Milestone 1 | Pending | 17 Nov | Each owner draws the class diagram for their packages by 15 Nov. |
| S-06 | Hold Asistensi 2 | Laura | No branch | Asistensi | Pending | 3 Dec | Book the session by 30 Nov. |
| S-07 | Booklet, A5, at most 15 pages | Areli | `docs/S-07-booklet` | Buklet | Pending | 3 Dec | Collect meeting screenshots and early sketches from now on for the development story. |
| S-08 | Activity log PDF | Areli | `docs/S-08-activity-log` | Log Activity | Pending | 4 Dec | Remind members to fill `docs/log-activity.md` weekly. |
| S-09 | Release and public repository | Laura | Tag `v1.0` on `main` | Milestone 2 | Pending | 4 Dec | Waits for S-07, S-08, S-10. |
| S-10 | Full integration test | All | `fix/<short-name>` per bug | Pedoman 10 | Pending | 2 Dec | Walk through every acceptance criterion in `PRD.md` section 4. |

## Core and MVP

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| C-01 | Core types and contracts | Laura | `feat/C-01-core-contracts` | F00.1 | Done | 12 Nov | 10 Nov: `make build` passes with `-Werror`, 35 tests green. Every contract except the plain interfaces `Renderer` and `InputSource` has a test. `TimerSystemTest` is the sample system test with no game loop. Signatures recorded in `ARCHITECTURE.md`. Needs review by Marshall, Ilham, and Areli. |
| C-02 | Game loop and threads | Laura | `feat/C-02-game-loop` | F09.3 | Done | 13 Nov | 10 Nov: `GameLoopTest` with a fake clock and uneven 1–40 ms frames shows 300 real seconds give 300 ± 1 game seconds, and a 2-second stall runs 5 ticks. `KeyBindingsTest` refuses a key bound twice. `CliInputTest` turns keys from `config/keybindings.properties` into commands. `GameSessionTest` shows the threads `input`, `game-loop`, and `render`, a key reaching the loop through the queue, and every thread stopped within 1 second. Rerun 5 times, all green. Not run in a real terminal until C-05. |
| C-03 | CLI renderer and HUD | Laura | `feat/C-03-cli-renderer` | F09.3, F07.2 | Done | 13 Nov | 10 Nov: `CliFrameTest` shows the `DESIGN.md` glyphs two characters per tile, the draw priority, and a HUD with lives, selected bomb, score, and a timer that turns red below 30 seconds. `CliRendererTest` on a JLine test terminal shows an identical frame writes nothing, a one-tile move rewrites two cells without clearing, a resize redraws everything, and a 40 × 10 terminal gets one message asking for 48 × 13. Not seen in a real terminal until C-05. |
| C-04 | Player movement and collision | Laura | `feat/C-04-player-movement` | F01.2.1, F01.2.3 | Done | 14 Nov | 10 Nov: `PlayerSystemTest` shows a held move covering 40 ± 1 tiles in 10 seconds at speed 4 and 60 ± 1 at speed 6, one tile per step, movement stopped by a wall, an enemy, an active bomb, and the map edge, a move 0.15 seconds early kept and one 0.20 seconds early dropped, and stepping into an enemy costing one life with a `PlayerDamaged` event while the two never share a tile. Enemy and bomb are test stand-ins until E-01 and B-01. |
| C-05 | MVP-0 walking skeleton | Laura | `feat/C-05-mvp0` | F09 | Done | 14 Nov | 11 Nov: `MvpZeroTest` on the real composition shows the 14 × 10 Red Mountain layout in MENU, moves ignored in the menu, Deeper walking after confirm and stopping at a block, pause freezing the timer and moves, LOSE at 0 seconds, and confirm quitting. `MainTest` plays menu → step → pause → quit through `Main.play` on a JLine test terminal and checks the terminal is given back, and refuses a dumb terminal. 79 tests green, app tests rerun 5 times. 11 Nov, by hand in Windows Terminal (`windows-vtp`): the map, HUD, and prompt draw without flicker, Deeper walks with WASD and the arrows and stops at blocks and enemies, the timer counts down, P pauses and resumes, and Backspace from the pause quits and gives the terminal back. |
| C-06 | Lives, invulnerability, respawn | Laura | `feat/C-06-lives-respawn` | F01.2.2, F01.2.4 | Done | 19 Nov | 11 Nov: `LifeSystemTest` shows one life lost per activation and per tick, a respawn at the spawn point, the nearest free tile by walking distance when the spawn is occupied, never a tile holding another entity, invulnerability lasting exactly 1.5 seconds after the respawn, one `PlayerDamaged` per life, and no respawn at 0 lives followed by LOSE. `MvpZeroTest` shows contact with a stand-in enemy sending Deeper back to the spawn point, invulnerable. `CliFrameTest` shows the dimmed player. 94 tests green. |
| C-07 | Menu, pause, restart, game over | Laura | `feat/C-07-game-flow` | F09.1, F09.5, F00.4 | Done | 20 Nov | 11 Nov: `GameFlowTest` shows the screen stack (How to Play pushed, back returns to the menu), up and down wrapping, number keys choosing, an invalid or unbound key showing an error while the menu stays open, pause offering Resume, Restart, and Main Menu, a held pause key toggling once, and the final score on the WIN and LOSE screens. `MvpZeroTest` shows a fresh world for restart with every initial value. `MainTest` plays menu, pause, Main Menu (a new session), and Exit through `Main.play` and checks the terminal is given back. `CliFrameTest` shows the boxed panel, the dimmed map, and the tallest menu fitting. 106 tests green, app tests rerun 5 times. Not yet seen in a real terminal. |
| C-08 | Win and lose check | Laura | `feat/C-08-win-lose` | F09.4 | Done | 16 Nov | 11 Nov: `WinLoseSystemTest` shows the exit staying locked until the 5 Normal Enemies and the boss are defeated, standing on a locked exit not winning, WIN only with the player on the open exit, LOSE at 0 seconds and at 0 lives (even on the open exit), and one `GameEnded` event with the lives and seconds for the score bonus. `MvpZeroTest` shows stand-in enemies costing lives until LOSE and keeping the exit locked. 84 tests green. Enemies are stand-ins until E-01. |
| C-09 | MVP-1 integration | Laura | `feat/C-09-mvp1` | F00.2 | Pending | 17 Nov | Waits for C-05, C-08, B-01, B-02, M-01, E-01. |

## Bomb, Explosion, Block, Power-Up, Score

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| B-01 | Bomb base and Normal Bomb | Marshall | `feat/B-01-normal-bomb` | F02.1, F02.2.1, F02.3, F01.2.5 | Pending | 15 Nov | Until C-01 lands, draw the class diagram for the bomb packages. |
| B-02 | Explosion spread and damage | Marshall | `feat/B-02-explosion` | F03 | Pending | 16 Nov | Starts after B-01 and B-03. |
| B-03 | Three block types | Marshall | `feat/B-03-blocks` | F04 | Pending | 15 Nov | Starts after C-01. Can be open alongside B-01. |
| B-04 | Pumped Bomb and type selection | Marshall | `feat/B-04-pumped-bomb` | F02.2.2, F02.3.2 | Pending | 20 Nov | Starts after B-01, B-02, B-03. |
| B-05 | Remote Bomb and detonate | Marshall | `feat/B-05-remote-bomb` | F02.2.3, F02.3.3 | Pending | 21 Nov | Starts after B-01. |
| B-06 | Bounded chain reactions | Marshall | `feat/B-06-chain-reaction` | F02.3.7, F03.2.6 | Pending | 23 Nov | Starts after B-02. |
| B-07 | Power-ups | Marshall | `feat/B-07-powerups` | F06 | Pending | 25 Nov | Starts after B-03. |
| B-08 | Score | Marshall | `feat/B-08-score` | F08 | Pending | 26 Nov | Starts after C-01. |

## Enemy and Boss

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| E-01 | Enemy base and Patrol Enemy | Ilham | `feat/E-01-patrol-enemy` | F05.1, F05.2 | Pending | 15 Nov | Until C-01 lands, draw the class diagram for the enemy and boss packages. |
| E-02 | Chase Enemy | Ilham | `feat/E-02-chase-enemy` | F05.2 | Pending | 19 Nov | Starts after E-01. |
| E-03 | Contact damage and enemy defeat | Ilham | `feat/E-03-enemy-contact` | F01.2.2, F03.2.5 | Pending | 20 Nov | Starts after E-01. |
| E-04 | Boss framework | Ilham | `feat/E-04-boss-framework` | F05.3.1 | Pending | 22 Nov | Starts after C-01. Can run in parallel with E-02. |
| E-05 | Hades phase 1 attacks | Ilham | `feat/E-05-hades-phase1` | F05.3.4 | Pending | 24 Nov | Starts after E-04. |
| E-06 | Hades phase 2 variants | Ilham | `feat/E-06-hades-phase2` | F05.3.4 | Pending | 26 Nov | Starts after E-05. |
| E-07 | Attacks stopped by blocks, warning tiles | Ilham | `feat/E-07-boss-blocking` | F05.3.1 | Pending | 26 Nov | Starts after E-05. |

## Map and environment

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| M-01 | Map format, loader, entity factory | Areli | `feat/M-01-map-loader` | F00.2, F07.1, F07.2 | Pending | 15 Nov | Until C-01 lands, draw the class diagram for the map packages. |
| M-02 | Map validator | Areli | `feat/M-02-map-validator` | F00.3 | Pending | 19 Nov | Starts after M-01. |
| M-03 | Tiles and Lava Tile cycle | Areli | `feat/M-03-lava-cycle` | F07.3.3 | Pending | 21 Nov | Starts after C-01. ERUPTING duration awaits PRD open question 2. |
| M-04 | Lava damage and warning tiles | Areli | `feat/M-04-lava-damage` | F07.3.3, F01.2.2 | Pending | 23 Nov | Starts after M-03. |
| M-05 | Configuration files | Areli | `feat/M-05-config` | F05.2, F06.3 | Pending | 24 Nov | Starts after M-01. |
| M-06 | How to Play text, README feature table | Areli | `feat/M-06-how-to-play` | F09.1.1 | Pending | 26 Nov | Can start any time. |

## GUI (bonus B01)

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| G-01 | libGDX window and renderer | Laura | `feat/G-01-gui-window` | B01 | Pending | 22 Nov | Starts after C-05. First pin the libGDX version and confirm an empty window opens on all four machines. |
| G-02 | GUI key input | Laura | `feat/G-02-gui-input` | B01 | Pending | 24 Nov | Starts after G-01. |
| G-03 | GUI menu, HUD, pause, game over | Laura | `feat/G-03-gui-screens` | B01 | Pending | 27 Nov | Starts after G-01 and C-07. |
| G-04 | Animation | Laura | `feat/G-04-gui-animation` | B01 | Pending | 1 Dec | Starts after G-01. |

## Other bonuses

| ID | Task | Owner | Branch | Spec | Status | Due | Next step or verification |
| --- | --- | --- | --- | --- | --- | --- | --- |
| X-01 | Five design patterns documented | Laura | `docs/X-01-design-patterns` | B05 | Pending | 1 Dec | Keep the pattern table in `ARCHITECTURE.md` current as classes are written. |
| X-02 | Bomb Kick | Marshall | `feat/X-02-bomb-kick` | B02 | Pending | 29 Nov | Starts after B-01. |
| X-03 | Map Maker | Areli | `feat/X-03-map-maker` | B03 | Pending | 1 Dec | Starts after M-02. |
| X-04 | Local Multiplayer | Marshall | `feat/X-04-local-multiplayer` | B04 | Pending | 1 Dec | Starts after C-06 and B-07. |
| X-05 | Creativity feature | Ilham | `feat/X-05-creativity` | BXX | Pending | 1 Dec | The team picks the feature by 20 Nov. |
