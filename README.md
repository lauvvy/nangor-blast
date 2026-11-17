# Nangor Blast

A real-time Bomberman game for the command line in Java, where one player guides Deeper through Red Mountain, defeats five enemies and the boss Hades, and reaches the exit before time runs out.

Course project for IF2010 Object-Oriented Programming 2026, map type C (Red Mountain).

## Problem Solved

The course asks each group to show object-oriented design in a working program: inheritance, interfaces, polymorphism, generics, exceptions, collections, and concurrency, together with design patterns and SOLID principles. Nangor Blast puts all of them into one game whose rules, timing, input, and display are kept in separate layers.

## Key Features

Nothing is implemented yet. The status column is updated as tasks in [`TODO.md`](docs/TODO.md) are completed.

| Feature | Description | Status |
| --- | --- | --- |
| F00 | Game initialization, map validation, restart | Not started |
| F01 | Player movement, lives, respawn | Not started |
| F02 | Normal, Pumped, and Remote Bomb | Not started |
| F03 | Explosion | Not started |
| F04 | Indestructible, Destructible, and Reinforced Block | Not started |
| F05 | Patrol and Chase enemies, boss Hades | Not started |
| F06 | Five power-ups | Not started |
| F07 | Red Mountain map with Lava Tiles | Not started |
| F08 | Score | Not started |
| F09 | Menu, game states, pause, game over | Not started |
| B01 | GUI (bonus) | Not started |
| B02 | Bomb Kick (bonus) | Not started |
| B03 | Map Maker (bonus) | Not started |
| B04 | Local Multiplayer (bonus) | Not started |
| B05 | Five design patterns (bonus) | Not started |
| BXX | Creativity feature (bonus), not yet chosen | Not started |

## Screenshot or Demo

A screenshot or demo is not yet available.

## Tech Stack and Rationale

- **Java 21** — the language the course requires, in its current long-term support release.
- **Gradle wrapper** — builds and packages the project without each member installing Gradle.
- **Make** — the specification requires a `Makefile` for build, run, and clean.
- **JLine 3** — reads single key presses and redraws the terminal in real time. Pending confirmation by a spike on the team's terminals.
- **libGDX** — the GUI bonus, chosen for sprites, animation, and window scaling. Only the GUI view uses it, so the command-line game runs without it.
- **SLF4J with Logback** — structured logging to a file, so logs do not disturb the game screen.
- **JUnit 5** — unit tests for the game rules.

The design is described in [`ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Installation and Setup

1. Install JDK 21 and `make`. Gradle is not needed: the wrapper downloads Gradle 8.14.3 on the first build. Check them with:

   ```bash
   java -version
   make --version
   ```

   On Windows, `make` can come from MSYS2 or WinLibs, where it may be named `mingw32-make`. Use that name in place of `make` below.

2. Clone the repository and enter it:

   ```bash
   git clone https://github.com/lauvvy/nangor-blast.git
   cd nangor-blast
   ```

3. Build, then run:

   ```bash
   make build
   make run
   ```

Other targets: `make test` runs the unit tests and `make clean` removes build output. `make run-gui` starts the GUI once bonus B01 is built (task `G-01`).

Logs are written to `logs/nangor-blast.log`, never to the screen.

## Team and Task Division

| Member | NIM | Responsibility |
| --- | --- | --- |
| Laura Paula Gosal | 18225060 | Core contracts, game loop and concurrency, input, CLI view, game flow, MVP, integration, GUI |
| Marshall Farrell Attalla K | 18225062 | Bomb, Explosion, Block, Power-Up, Score, Bomb Kick, Local Multiplayer |
| Muhammad Ilham Malik Amrullah | 18225022 | Normal Enemy, boss framework, Hades, creativity feature |
| Areli Matthew Widodo | 18225102 | Map loading and validation, Lava Tile, configuration, Map Maker, booklet, activity log |

Task-level detail is in [`TODO.md`](docs/TODO.md).

## Project Documents

| Document | Purpose |
| --- | --- |
| [`PRD.md`](docs/PRD.md) | Scope, user stories, and acceptance criteria |
| [`ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Stack, structure, contracts, and the implementation plan |
| [`TODO.md`](docs/TODO.md) | Task status for the whole team |
| [`WORKFLOW.md`](docs/WORKFLOW.md) | Working rules, Git workflow, and definition of done |
| [`DESIGN.md`](docs/DESIGN.md) | Terminal glyphs and GUI visual rules |
| [Integrated Class Diagram](docs/diagram/Class%20Diagram%20Terintegrasi%20Nangor%20Blast.drawio) | Combined class diagram from all four source diagrams, with an overview and six package detail pages |
| [`CLAUDE.md`](CLAUDE.md) | Instructions for the coding agent |
| [`IF2010 Spesifikasi Tugas Besar.md`](docs/IF2010%20Spesifikasi%20Tugas%20Besar.md) | The course specification |
| [`log-activity.md`](docs/log-activity.md) | Activity log per member, for Milestone 2 |
