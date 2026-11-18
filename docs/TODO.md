# Project Tasks — Nangor Blast

**Plan revision:** 19 November 2026. **Code baseline:** main at `267d10b`.

The active execution plan assigns future work to **Laura, Marshall, and Ilham**. Areli's nine unfinished parent tasks are split/reassigned below; past credits, logs, and the four-person roster are preserved. Ownership and recovery dates in this file and the matching implementation plan supersede the original allocation.

## Working rules and current evidence

- Status values: **Pending**, **In progress**, **Blocked**, **Done**. Moving a task does not complete it. Preserve previous verification notes; obtain tests, manual checks where relevant, and review before closing a row.
- The last available regression reports are from **17 November**: 141 tests, no failures/errors/skips. B-04/B-05/B-06 have code on main but still need their remaining review/acceptance cases; E-01 remains open for verification. This planning update does not rerun or claim new game tests.
- Map/mapio/hazard/config/editor production modules and the power-up/score/boss work are still open. Diagrams are design input, not implementation evidence.
- S-03/S-04/S-05 retain their historical deadlines. Verify external assistance/submission evidence; do not silently mark them Done or move the official Milestone 1 deadline.
- All dates below are in **2026, WIB**. A target is an internal plan, not proof that a task was submitted.
- Each leaf task has one implementation owner; S-10 is explicitly shared, with **3 SP per contributor**. Only the named file owner edits a shared module, apart from reviewed wiring changes stated below.
- Use one branch per leaf task. Parent groups M-01/M-02/M-04/M-05/M-06/S-07/X-03 close only when **all children are Done**; they are not extra assignments or branches. M-03 and S-08 keep their original IDs.
- **MANDATORY** means S-02 and every C/B/E/M leaf task is Done. It excludes documentation S-07/S-08 and all G/X bonuses. Protect the 26 Nov recovery target; GUI is scheduled from 27 Nov and other bonuses wait for this actual gate. If a dependency slips, update the forecast and cut/defer bonus work through the existing workflow.
- Update only the task evidence actually checked; log your own work in [log-activity.md](log-activity.md). API/factory changes and composition order require review from someone other than the author.

## Workload balance

SP are **remaining-effort estimates**, not hours or completed work: 1 = small verification/documentation, 2 = focused module, 3 = algorithm/integration, 4–5 = substantial feature, 8 = cross-system feature. Estimates include the task's tests/review. Re-estimate after C-09 and after X-05 is chosen. S-10 is counted once per person at 3 SP; Done rows count as zero. The original-work estimates are visible on their rows.

| Contributor | Existing remaining SP | Reassigned packages | Additional SP | Total remaining SP | Reassigned task IDs |
| --- | --- | --- | --- | --- | --- |
| Laura | 30 | 6 | 13 | 43 | `M-01A`, `M-01B`, `M-02B`, `M-04A`, `M-05B`, `S-07A` |
| Marshall | 27 | 6 | 16 | 43 | `M-01C`, `M-02A`, `M-03`, `M-05A`, `M-06B`, `X-03A` |
| Ilham | 32 | 6 | 10 | 42 | `M-04B`, `M-06A`, `S-07B`, `S-07C`, `S-08`, `X-03B` |

Each contributor receives six packages. Marshall receives more new implementation effort because three bomb features already have code; Ilham receives less new implementation and more work after the boss deadline because the enemy/boss lane is still substantial. Laura takes the loader/reachability/wiring work beside her existing core and GUI responsibilities. The total forecast is **43 / 43 / 42 SP**, not an assertion of equal calendar availability.

## Coverage of Areli's previous allocation

| Former parent | New leaf tasks | Coverage / handoff |
| --- | --- | --- |
| M-01 | `M-01A`, `M-01B`, `M-01C` | Laura owns the format and loader; Marshall owns entity creation. |
| M-02 | `M-02A`, `M-02B` | Marshall owns structural validation; Laura owns the reachability helper. |
| M-03 | `M-01A`, `M-03` | Base tiles move to M-01A; Marshall retains the complete lava-cycle responsibility. |
| M-04 | `M-04A`, `M-04B` | Laura advances/publishes warnings; Ilham applies damage in a separate system. |
| M-05 | `M-05A`, `M-05B` | Marshall supplies validated config; Laura coordinates injection and Remote obtainability. |
| M-06 | `M-06A`, `M-06B` | Ilham updates help; Marshall reconciles README status/setup. |
| S-07 | `S-07A`, `S-07B`, `S-07C` | Laura supplies the manual; Ilham compiles technical content and the reviewed PDF. |
| S-08 | `S-08` | Ilham compiles the log; every author supplies their own entries. |
| X-03 | `X-03A`, `X-03B` | Marshall builds editing/validation UI; Ilham builds the save/load helper. |

## Shared-file ownership and review seams

| File / area | Primary owner | Other contributor's handoff |
| --- | --- | --- |
| GameMap, Tile, FloorTile; Red Mountain map file | Laura | Marshall consumes the symbol/constructor contract in M-01C/M-03. |
| MapLoader and ReachabilityValidator | Laura | Marshall reviews the one delegation call in his MapValidator. |
| MapValidator, EntityFactory, GameConfig; game.properties | Marshall | Laura supplies reachability and config-wiring integration; coordinate APIs before dependent branches. |
| LavaTile and LavaState | Marshall | Laura/Ilham consume state, dt progression, and stable eruption IDs. |
| HazardSystem | Laura | Calls the model's cycle logic and manages warning overlays. |
| LavaDamageSystem | Ilham | Runs after HazardSystem, before LifeSystem; does not edit Laura's warning loop. |
| GameFlow How to Play text | Laura owns the file; Ilham owns the M-06A text change | Limit the edit to help text and obtain Laura's review. |
| MapMaker editing / MapMakerIO persistence | Marshall / Ilham | Separate files and one agreed rows/validation handoff. |
| app/GameFactory | Laura coordinates | Other owners register only the reviewed system/factory wiring for their task. |
| Final booklet / log PDFs | Ilham assembles | Laura supplies manual/screenshots; all contributors review their own content and preserve previous credits. |

## Dates and recovery order

| Date | Checkpoint |
| --- | --- |
| 17 Nov, 20.10 | Historical Milestone 1 deadline; verify S-05 evidence. |
| 20 Nov | Map/base-tile and configuration contracts (M-01A/M-05A); terminal evidence (S-02). |
| 21–22 Nov | Both validator stages, lava model, and boss framework; agree entity constructors. |
| 23–25 Nov | Warning/damage systems, real entity population, then file loader. |
| 26 Nov | C-09 plus config/Remote obtainability integration; all mandatory CLI criteria. This path has little slack: M-01C → M-01B → C-09 is critical. |
| 27 Nov–1 Dec | Bonus work after the mandatory gate; 1 Dec is the existing bonus cut-off. |
| 2 Dec | S-10 final integration/freeze; each contributor checks their allocated surface. |
| 3 Dec | Asistensi 2, reviewed A5 booklet, internal activity-log PDF handoff. |
| 4 Dec, 18.00 | Internal release target; official Milestone 2 deadline remains **20.10 WIB**. |

## Setup and deliverables

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| S-01 | Scaffold Gradle, Makefile, folders | Laura | 0 | `feat/S-01-scaffold` | Milestone 1 | Done | — | 11 Nov | 10 Nov: on Windows with `mingw32-make`, `make build`, `make test` (1 test), `make run` (prints placeholder), and `make clean` pass with `-Xlint:all -Werror`. Logs go to `logs/nangor-blast.log`. `src`, `config`, `doc`, and the root `README.md` exist. Not yet checked on macOS or Linux. |
| S-02 | Terminal spike with JLine 3 | Laura | 2 | `feat/S-02-terminal-spike` | Pedoman 3 | In progress | `S-01` | 20 Nov | Recovery target from 12 Nov. Record raw-key, redraw, key-release, and two-key results for the three active contributors; preserve earlier results. Manual checks remain open. |
| S-03 | Choose assistant, invite to repo, book Asistensi 1 | Laura | 1 | No branch | Asistensi | Pending | — | 11 Nov | Historical deadline: verify the assistant sheet, repository access, and booking evidence. No completion is inferred from the date; retain the actual submission date. |
| S-04 | Hold Asistensi 1 | Laura | 1 | No branch | Asistensi | Pending | `S-03` | 16 Nov | Historical deadline: verify assistance notes, resolved PRD questions, attendance, and forms before closing. Do not record an unverified meeting as complete. |
| S-05 | Milestone 1 PDFs | Laura | 2 | `docs/S-05-milestone-1` | Milestone 1 | Pending | `C-01` | 17 Nov | Historical deadline. Four source diagrams and the combined draw.io file are merged. Final PDFs/submission evidence are not tracked in doc/; verify what was submitted. Do not infer completion from diagram availability. |
| S-06 | Hold Asistensi 2 | Laura | 1 | No branch | Asistensi | Pending | — | 3 Dec | Book the session by 30 Nov. |
| S-07A | Booklet gameplay, controls, and screenshots | Laura | 1 | `docs/S-07A-booklet-manual` | Buklet | Pending | `C-07`, `M-06A` | 29 Nov | Write the cover/roster, gameplay/user manual, and screenshots from the actual build. Include current controls and all four members' original credits; describe the three-person execution plan without rewriting earlier contributions. Hand editable material to Ilham. |
| S-07B | Booklet technical content and final diagrams | Ilham | 2 | `docs/S-07B-booklet-technical` | Buklet | Pending | `C-09`, `E-07` | 1 Dec | Collect each owner's explanation of OOP, at least two actual design patterns, at least three SOLID principles, task division, and development history. Refresh the final class diagram from implemented code; add the optional five-pattern result only if X-01 is complete. Do not invent contributions or block the mandatory booklet on that bonus. |
| S-07C | Booklet assembly, team review, and final PDF | Ilham | 2 | `docs/S-07C-booklet-pdf` | Buklet | Pending | `S-07A`, `S-07B`, `S-10` | 3 Dec | Combine the drafts, apply A5 layout, keep at most 15 pages, and export the final PDF to doc/. Laura and Marshall review their sections and links/legibility; retain their review evidence before the release handoff. |
| S-08 | Activity-log collection and final PDF | Ilham | 1 | `docs/S-08-activity-log` | Log Activity | Pending | `S-07C`, `S-10` | 3 Dec | Collect entries from every member, preserve historical Areli entries, and ask authors to fill their own missing evidence. Export one PDF to doc/ and verify its contents. Refresh the snapshot if work continues before the 4 Dec release; 3 Dec is the internal target, 4 Dec 20.10 WIB is the official submission deadline. |
| S-09 | Release and public repository | Laura | 1 | Tag `v1.0` on `main` | Milestone 2 | Pending | `S-07C`, `S-08`, `S-10` | 4 Dec | Release before the internal 18.00 WIB target on 4 Dec, ahead of the official 20.10 deadline. Include source, reviewed booklet, and log PDF; verify repository visibility and the form. |
| S-10 | Full integration test | Laura + Marshall + Ilham | 3 each | `fix/<short-name>` per bug | Pedoman 10 | Pending | `MANDATORY` | 2 Dec | 3 SP per contributor: Laura checks map/input/flow/GUI; Marshall checks bombs/blocks/power-ups/score/editor; Ilham checks enemy/boss/lava damage and cross-feature interactions. Record manual results and assign every defect. |

## Core and MVP

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| C-01 | Core types and contracts | Laura | 0 | `feat/C-01-core-contracts` | F00.1 | Done | `S-01` | 12 Nov | 10 Nov: `make build` passes with `-Werror`, 35 tests green. Every contract except the plain interfaces `Renderer` and `InputSource` has a test. `TimerSystemTest` is the sample system test with no game loop. Signatures recorded in `ARCHITECTURE.md`. Needs review by Marshall, Ilham, and Areli. |
| C-02 | Game loop and threads | Laura | 0 | `feat/C-02-game-loop` | F09.3 | Done | `C-01` | 13 Nov | 10 Nov: `GameLoopTest` with a fake clock and uneven 1–40 ms frames shows 300 real seconds give 300 ± 1 game seconds, and a 2-second stall runs 5 ticks. `KeyBindingsTest` refuses a key bound twice. `CliInputTest` turns keys from `config/keybindings.properties` into commands. `GameSessionTest` shows the threads `input`, `game-loop`, and `render`, a key reaching the loop through the queue, and every thread stopped within 1 second. Rerun 5 times, all green. Not run in a real terminal until C-05. |
| C-03 | CLI renderer and HUD | Laura | 0 | `feat/C-03-cli-renderer` | F09.3, F07.2 | Done | `C-01`, `S-02` | 13 Nov | 10 Nov: `CliFrameTest` shows the `DESIGN.md` glyphs two characters per tile, the draw priority, and a HUD with lives, selected bomb, score, and a timer that turns red below 30 seconds. `CliRendererTest` on a JLine test terminal shows an identical frame writes nothing, a one-tile move rewrites two cells without clearing, a resize redraws everything, and a 40 × 10 terminal gets one message asking for 48 × 13. Not seen in a real terminal until C-05. |
| C-04 | Player movement and collision | Laura | 0 | `feat/C-04-player-movement` | F01.2.1, F01.2.3 | Done | `C-01` | 14 Nov | 10 Nov: `PlayerSystemTest` shows a held move covering 40 ± 1 tiles in 10 seconds at speed 4 and 60 ± 1 at speed 6, one tile per step, movement stopped by a wall, an enemy, an active bomb, and the map edge, a move 0.15 seconds early kept and one 0.20 seconds early dropped, and stepping into an enemy costing one life with a `PlayerDamaged` event while the two never share a tile. Enemy and bomb are test stand-ins until E-01 and B-01. |
| C-05 | MVP-0 walking skeleton | Laura | 0 | `feat/C-05-mvp0` | F09 | Done | `C-02`, `C-03`, `C-04` | 14 Nov | 11 Nov: `MvpZeroTest` on the real composition shows the 14 × 10 Red Mountain layout in MENU, moves ignored in the menu, Deeper walking after confirm and stopping at a block, pause freezing the timer and moves, LOSE at 0 seconds, and confirm quitting. `MainTest` plays menu → step → pause → quit through `Main.play` on a JLine test terminal and checks the terminal is given back, and refuses a dumb terminal. 79 tests green, app tests rerun 5 times. 11 Nov, by hand in Windows Terminal (`windows-vtp`): the map, HUD, and prompt draw without flicker, Deeper walks with WASD and the arrows and stops at blocks and enemies, the timer counts down, P pauses and resumes, and Backspace from the pause quits and gives the terminal back. |
| C-06 | Lives, invulnerability, respawn | Laura | 0 | `feat/C-06-lives-respawn` | F01.2.2, F01.2.4 | Done | `C-04` | 19 Nov | 11 Nov: `LifeSystemTest` shows one life lost per activation and per tick, a respawn at the spawn point, the nearest free tile by walking distance when the spawn is occupied, never a tile holding another entity, invulnerability lasting exactly 1.5 seconds after the respawn, one `PlayerDamaged` per life, and no respawn at 0 lives followed by LOSE. `MvpZeroTest` shows contact with a stand-in enemy sending Deeper back to the spawn point, invulnerable. `CliFrameTest` shows the dimmed player. 94 tests green. |
| C-07 | Menu, pause, restart, game over | Laura | 0 | `feat/C-07-game-flow` | F09.1, F09.5, F00.4 | Done | `C-02` | 20 Nov | 11 Nov: `GameFlowTest` shows the screen stack (How to Play pushed, back returns to the menu), up and down wrapping, number keys choosing, an invalid or unbound key showing an error while the menu stays open, pause offering Resume, Restart, and Main Menu, a held pause key toggling once, and the final score on the WIN and LOSE screens. `MvpZeroTest` shows a fresh world for restart with every initial value. `MainTest` plays menu, pause, Main Menu (a new session), and Exit through `Main.play` and checks the terminal is given back. `CliFrameTest` shows the boxed panel, the dimmed map, and the tallest menu fitting. 106 tests green, app tests rerun 5 times. Not yet seen in a real terminal. |
| C-08 | Win and lose check | Laura | 0 | `feat/C-08-win-lose` | F09.4 | Done | `C-01` | 16 Nov | 11 Nov: `WinLoseSystemTest` shows the exit staying locked until the 5 Normal Enemies and the boss are defeated, standing on a locked exit not winning, WIN only with the player on the open exit, LOSE at 0 seconds and at 0 lives (even on the open exit), and one `GameEnded` event with the lives and seconds for the score bonus. `MvpZeroTest` shows stand-in enemies costing lives until LOSE and keeping the exit locked. 84 tests green. Enemies are stand-ins until E-01. |
| C-09 | MVP-1 integration | Laura | 3 | `feat/C-09-mvp1` | F00.2 | Pending | `C-05`, `C-08`, `B-01`, `B-02`, `E-01`, `E-03`, `M-01B`, `M-05B` | 26 Nov | Recovery target from 17 Nov. Replace StandInMap with the file-based pipeline. On the real Red Mountain map, a bomb destroys a block and a Patrol Enemy; verify WIN, LOSE, pause, and restart together. |

## Bomb, Explosion, Block, Power-Up, Score

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| B-01 | Bomb base and Normal Bomb | Marshall | 0 | `feat/B-01-normal-bomb` | F02.1, F02.2.1, F02.3, F01.2.5 | Done | `C-01` | 15 Nov | 12 Nov: `make build` passes with `-Werror`, 114 tests green. `BombSystemTest` shows a 3-second fuse, placement refused at full capacity, on a tile with a bomb, and outside PLAYING, capacity returned, blast range fixed at placement, and the player leaving the bomb tile but not re-entering it. Capacity returns when the bomb goes off; B-02 moves it to the end of the explosion. Reviewed by Ilham in PR #11. |
| B-02 | Explosion spread and damage | Marshall | 0 | `feat/B-02-explosion` | F03 | Done | `B-01`, `B-03` | 16 Nov | 15 Nov: `make build` passes with `-Werror`, 133 tests green. `ExplosionSystemTest` shows the center and four arms, arms stopping at the map edge and at each block type (Destructible destroyed, Reinforced only by a Pumped Bomb, neither tile in the area), a 0.5-second duration, power-ups unaffected, `takeDamage` called once per target, the owner not immune, an invulnerable player unhurt, capacity returned when the explosion ends, and another bomb in the area triggered. Power-up drops from blocks come in B-07, bounded chains in B-06. Reviewed by Marshall with Ilham, pairing in person. Merged in PR #15. |
| B-03 | Three block types | Marshall | 0 | `feat/B-03-blocks` | F04 | Done | `C-01` | 15 Nov | 12 Nov: `make build` passes with `-Werror`, 122 tests green. `BlockTest` shows every type blocking every mover, Indestructible never destroyed, Destructible destroyed by every bomb, Reinforced only by a Pumped Bomb, a destroyed block's tile walkable again, and no bomb on an Indestructible tile. `TestWorlds` now builds `#` as an `IndestructibleBlock`. The power-up a Destructible Block may release comes in B-07. Reviewed by Marshall with Ilham, pairing in person. |
| B-04 | Pumped Bomb and type selection | Marshall | 1 | `feat/B-04-pumped-bomb` | F02.2.2, F02.3.2 | In progress | `B-01`, `B-02`, `B-03`, `E-04` | 23 Nov | PumpedBomb, factory selection, and command handling are on main (601bebe). The 17 Nov regression report covers fuse/range/damage values and selection. Finish the real Reinforced Block/boss integration cases and review before Done. |
| B-05 | Remote Bomb and detonate | Marshall | 2 | `feat/B-05-remote-bomb` | F02.2.3, F02.3.3 | In progress | `B-01`, `B-07` | 26 Nov | RemoteBomb and owned-bomb detonation are on main (601bebe). The 17 Nov regression report covers no fuse, lock guard, ownership, and paused commands. Add no-active-bomb and actual Remote Detonator pickup checks; obtain review. |
| B-06 | Bounded chain reactions | Marshall | 1 | `feat/B-06-chain-reaction` | F02.3.7, F03.2.6 | In progress | `B-02` | 23 Nov | Same-tick chain resolution is on main (601bebe); the two-bomb regression passes in the 17 Nov report. Add the dense-map bound/single-explosion test and review before Done. |
| B-07 | Power-ups | Marshall | 5 | `feat/B-07-powerups` | F06 | Pending | `B-03`, `M-05A` | 25 Nov | Implement five effects/caps, seeded drops, and guaranteed Remote Detonator placement. Agree config keys with M-05A; provide the obtainability hook/test used by M-05B. |
| B-08 | Score | Marshall | 3 | `feat/B-08-score` | F08 | Pending | `C-01`, `C-08`, `E-03`, `E-05` | 26 Nov | Use EventBus observers for actual EnemyDefeated/GameEnded events; ScoreSystem has no tick slot. Check 100 per Normal Enemy, 500 for Hades, WIN-only life/time bonuses, and restart reset without double counting. |

## Enemy and Boss

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| E-01 | Enemy base and Patrol Enemy | Ilham | 1 | `feat/E-01-patrol-enemy` | F05.1, F05.2 | In progress | `C-01` | 19 Nov | Patrol code is merged; the saved 17 Nov EnemySystemTest report has 3 passing cases. Reconcile review/manual verification evidence before closing. These tests were not rerun for this planning change. |
| E-02 | Chase Enemy | Ilham | 4 | `feat/E-02-chase-enemy` | F05.2 | Pending | `E-01` | 21 Nov | Starts after E-01. |
| E-03 | Contact damage and enemy defeat | Ilham | 5 | `feat/E-03-enemy-contact` | F01.2.2, F03.2.5 | Pending | `E-01` | 21 Nov | Starts after E-01. |
| E-04 | Boss framework | Ilham | 5 | `feat/E-04-boss-framework` | F05.3.1 | Pending | `C-01` | 22 Nov | Starts after C-01. Can run in parallel with E-02. |
| E-05 | Hades phase 1 attacks | Ilham | 5 | `feat/E-05-hades-phase1` | F05.3.4 | Pending | `E-04` | 24 Nov | Starts after E-04. |
| E-06 | Hades phase 2 variants | Ilham | 3 | `feat/E-06-hades-phase2` | F05.3.4 | Pending | `E-05` | 26 Nov | Starts after E-05. |
| E-07 | Attacks stopped by blocks, warning tiles | Ilham | 3 | `feat/E-07-boss-blocking` | F05.3.1 | Pending | `E-05` | 26 Nov | Starts after E-05. |

## Map, environment, configuration, and help — redistributed

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| M-01A | Map contract, base tiles, and Red Mountain file | Laura | 3 | `feat/M-01A-map-contract` | F00.2, F07.1, F07.2 | Pending | `C-01` | 20 Nov | Define the shared legend/row/position contract; create GameMap, Tile, FloorTile, and config/maps/red-mountain.txt. Use the existing 0-based Position API and 14 x 10 Red Mountain layout. Tests cover base-tile walkability and symbol/coordinate handling. Freeze factory/loader signatures with Marshall before their branches start. |
| M-01B | MapLoader and file error handling | Laura | 2 | `feat/M-01B-map-loader` | F00.2, F07.1, F07.2 | Pending | `M-01A`, `M-01C`, `M-02A`, `M-02B` | 25 Nov | Read UTF-8 rows, run both validation stages, then delegate to EntityFactory. Test a valid Red Mountain file, unreadable files, unknown symbols, malformed rows, and a useful error message. No world is started from invalid data. |
| M-01C | EntityFactory and spawn/config integration | Marshall | 5 | `feat/M-01C-entity-factory` | F00.2, F07.2 | Pending | `M-01A`, `M-03`, `M-05A`, `B-03`, `E-02`, `E-03`, `E-05` | 24 Nov | Build Player, five configured Patrol/Chase enemies, Hades, blocks, lava, and Exit from the legend. Apply configured patrol paths and the enemy split. Verify every spawn/type/position on the 14 x 10 fixture. Coordinate Hades construction with E-05; do not ship test stand-ins. |
| M-02A | MapValidator: dimensions, symbols, counts, and spawns | Marshall | 2 | `feat/M-02A-map-shape-validator` | F00.3 | Pending | `M-01A` | 21 Nov | Pure row validation: one test per size/count/spawn rule, overlapping or blocked spawns, and unknown symbols. InvalidMapException must name the broken rule. Own the MapValidator entry point; reserve one delegation seam for Laura's reachability helper. |
| M-02B | ReachabilityValidator: boss/exit paths and completability | Laura | 3 | `feat/M-02B-map-reachability` | F00.3 | Pending | `M-02A` | 22 Nov | Implement the separate ReachabilityValidator helper. Test unreachable boss/exit and impossible layouts, accounting for destructible/reinforced blocks and passable lava under F00.3. Add one delegation call to MapValidator, reviewed by Marshall; keep the algorithm outside his file. |
| M-03 | LavaTile/LavaState cycle | Marshall | 3 | `feat/M-03-lava-cycle` | F07.3.3 | Pending | `M-01A`, `M-05A` | 22 Nov | Build on Laura's base tiles. Test DORMANT 3 s, WARNING 1 s, configurable ERUPTING, and stable eruption IDs. Use dt only. PRD question 2 still has a working assumption of 1 s, synchronized tiles, player-only damage; keep it configurable until confirmed. |
| M-04A | HazardSystem: advance lava and publish warnings | Laura | 2 | `feat/M-04A-lava-warnings` | F07.3.3 | Pending | `M-03`, `C-03` | 23 Nov | Own HazardSystem for cycle advancement and GameWorld warning overlays. Test exact WARNING duration, removal of old warnings, and no cycle progress during PAUSED in a real GameLoop fixture. Register it before LavaDamageSystem and LifeSystem. |
| M-04B | LavaDamageSystem: damage per eruption and respawn integration | Ilham | 2 | `feat/M-04B-lava-damage` | F07.3.3, F01.2.2 | Pending | `M-04A`, `E-03`, `C-06` | 24 Nov | Use a separate LavaDamageSystem file and the stable eruption ID from M-03. Test at most one life per eruption, invulnerable/no-damage cases, entering/leaving lava, and respawn via LifeSystem. Register after HazardSystem and before LifeSystem; agree the seam with Laura. |
| M-05A | GameConfig parser, defaults, and validation | Marshall | 2 | `feat/M-05A-game-config` | F00.2, F05.2, F06.3, F07.3.3 | Pending | `C-01` | 20 Nov | Create validated config/game.properties loading and named defaults, including timing/speed/drop values, patrol paths, the Patrol/Chase split, and guaranteed-Remote settings. Test missing/invalid/out-of-range values; ConfigLoadException must name the key. Freeze keys with Laura and Ilham. |
| M-05B | Inject config and verify guaranteed Remote obtainability | Laura | 2 | `feat/M-05B-config-wiring` | F00.2, F05.2, F06.3 | Pending | `M-05A`, `M-01C`, `B-07` | 26 Nov | Wire validated values through factories/systems and remove the covered runtime literals with each domain owner. Models must not import mapio or read files. Integration tests change config values and verify they take effect, and prove the guaranteed Remote Detonator remains obtainable. |
| M-06A | How to Play goals and controls | Ilham | 1 | `feat/M-06A-how-to-play` | F09.1.1 | Pending | `C-07`, `B-07` | 26 Nov | Update help text for the goal, selected bomb, Pumped/Remote behavior, pause, and restart. Generate displayed keys from KeyBindings. Check help against real actions and the Remote unlock condition; update the screenshot/manual inputs for S-07A. Limit edits in Laura's GameFlow to the help-text section and obtain her review. |
| M-06B | README setup and verified feature status | Marshall | 1 | `feat/M-06B-readme-status` | README, F09.1.1 | Pending | `B-07`, `B-08`, `E-07` | 26 Nov | Reconcile every mandatory/bonus feature with merged code and recorded verification. Refresh build/run/config instructions. Keep incomplete or unverified features explicit; do not mark the whole game complete merely because code compiles. |

## GUI bonus — Laura

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| G-01 | libGDX window and renderer | Laura | 5 | `feat/G-01-gui-window` | B01 | Pending | `C-05`, `MANDATORY` | 27 Nov | Scheduled after the mandatory recovery gate. Pin libGDX and verify the window, resize, CLI compatibility, and clean shutdown on the active contributors' machines. |
| G-02 | GUI key input | Laura | 2 | `feat/G-02-gui-input` | B01 | Pending | `G-01` | 28 Nov | Starts after G-01. |
| G-03 | GUI menu, HUD, pause, game over | Laura | 4 | `feat/G-03-gui-screens` | B01 | Pending | `G-01`, `C-07` | 30 Nov | Starts after G-01 and C-07. |
| G-04 | Animation | Laura | 3 | `feat/G-04-gui-animation` | B01 | Pending | `G-01` | 1 Dec | Starts after G-01. |

## Other bonuses

| ID | Task | Owner | SP left | Branch | Spec | Status | Depends on | Target | Next step / verification |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| X-01 | Five design patterns documented | Laura | 2 | `docs/X-01-design-patterns` | B05 | Pending | `MANDATORY` | 1 Dec | Document five patterns actually used after mandatory checks. Supply evidence of the mandatory minimum of two to S-07B even if this five-pattern bonus remains unfinished. |
| X-02 | Bomb Kick | Marshall | 4 | `feat/X-02-bomb-kick` | B02 | Pending | `B-01`, `MANDATORY` | 29 Nov | Starts after B-01. |
| X-03A | Map Maker editing UI and validation feedback | Marshall | 3 | `feat/X-03A-map-editor` | B03 | Pending | `M-02A`, `M-02B`, `MANDATORY` | 29 Nov | Implement cursor/paint/erase editing and reuse the validators for readable rule errors. Keep editing separate from a running GameWorld. Test edits and refusal of invalid maps. Agree the rows/validation handoff with Ilham before X-03B. |
| X-03B | Map Maker save/load and playable round trip | Ilham | 2 | `feat/X-03B-map-editor-io` | B03 | Pending | `X-03A`, `M-01B`, `MANDATORY` | 1 Dec | Own persistence in a separate MapMakerIO helper. Reuse MapLoader/validators; a saved valid map reloads with the same layout and can be played. Test invalid input, unreadable files, and save failures with useful messages; keep validation and IO outside Marshall's editor UI file. |
| X-04 | Local Multiplayer | Marshall | 8 | `feat/X-04-local-multiplayer` | B04 | Pending | `C-06`, `B-07`, `G-02`, `MANDATORY` | 1 Dec | Keep the original co-op scope. Reuse the editor/map contracts; verify both players moving simultaneously in the GUI after G-02, plus the eight multiplayer rules. |
| X-05 | Creativity feature | Ilham | 3 | `feat/X-05-creativity` | BXX | Pending | `MANDATORY` | 1 Dec | Select a feature by 20 Nov. 3 SP is provisional, not an approved implementation scope; write measurable criteria and rebalance if the chosen feature needs more effort. |
