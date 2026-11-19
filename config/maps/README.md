# Map file format

`red-mountain.txt` is the F07.3.3 Red Mountain layout: 14 columns by 10 rows,
with one player, five Normal Enemies, one boss, one exit, eight Lava Tiles,
and four Destructible Blocks.

Maps are UTF-8 text with one row per line. LF and CRLF line endings and a
single UTF-8 BOM at the beginning are supported. A final newline is optional.
Rows must be nonempty and have equal widths. Spaces, blank lines, headers,
and comments are not part of the format; `#` always means a wall.

| Symbol | Initial contents |
| --- | --- |
| `.` | `FloorTile` terrain, held in `GameMap` |
| `#` | `IndestructibleBlock` |
| `D` | `DestructibleBlock` |
| `R` | `ReinforcedBlock` |
| `P` | Player zero at its spawn, with the model's initial stats |
| `E` | Normal Enemy from the configured enemy constructor |
| `B` | Boss from the configured boss constructor |
| `H` | Lava Tile from the configured lava constructor, initially dormant |
| `X` | Closed `Exit` |

Other map types, bombs, explosions, and power-ups cannot be placed in this
initial map format. Reinforced Blocks are supported by the legend, although
the supplied Red Mountain layout contains none.

The top-left tile is `Position(0, 0)`. Columns increase to the right and rows
increase downwards. Error messages use one-based row and column numbers.

## Loading

`mapio.MapLoader` reads the file and returns a new `GameWorld` in `PLAYING`,
with score zero, the supplied timer, and exactly one player. The filename
without its extension is the world's map name (`red-mountain` for the
supplied file). `parse(name, rows)` accepts a caller-supplied display name
and the same format in memory. Both entry points have an overload accepting
a `Random` instance, which is retained by the world for deterministic tests.

Following `docs/diagram/Class Diagram Map.drawio`, the loader separates
`readRows(path)`, validation through its `MapValidator`, and
`EntityFactory.populate(world, rows)`. `readRows` only decodes UTF-8 text;
`parse` removes one initial BOM, copies the rows, and validates them before
constructing the world. The validator currently enforces M-01's structural
rules; the full gameplay rules remain M-02.

`EntityFactory.create(symbol, position)` returns an `Entity` for every
supported symbol, including `FloorTile` for `.`. `populate` accepts a fresh
world and normalized rows, checks the dimensions and player spawn, prepares
all results, and then installs terrain and occupants. A failed creator does
not install a partial map or duplicate the player.

`world.map()` exposes `GameMap`, which owns a `Grid<Tile>`. `Tile` extends
`Entity`, and `FloorTile` extends `Tile`. Terrain stays at its grid
coordinates. Every cell has floor beneath its occupant unless it contains
lava. `tileAt(position)` reads terrain; `isWalkable(position)` checks terrain
and bounds. `world.isBlockedFor(mover, position)` also checks blocks and
actors occupying the cell.

Floor terrain does not appear in `world.entitiesAt`, so a floor tile does
not prevent respawn. Lava is the same object in both the terrain grid and
the gameplay entity list, making it available to hazard systems.
`GameMap.lavaTiles()` returns an immutable snapshot in row order; its
temporary return type is `List<Tile>` until M-03 supplies the concrete
`LavaTile` used by the diagram's `List<LavaTile>` signature.

The composition root supplies player speed, time limit, and three entity
creation functions to keep configuration and game rules out of the parser:

```java
EntityFactory entities = new EntityFactory(playerSpeed, enemyCreator, bossCreator, lavaCreator);
MapLoader loader = new MapLoader(entities, timeLimit);
GameWorld world = loader.load(Path.of("config", "maps", "red-mountain.txt"), random);
```

Each creator is a `Function<Position, ? extends Entity>` and must return a
fresh, active entity at the requested position. The enemy and boss must
have the matching `EntityKind` and implement `Hostile`; lava must extend
`Tile` and have kind `LAVA_DORMANT`. The enemy creator can select a configured movement strategy
per spawn. Missing creators fail at construction; incorrect results or
creator failures produce `InvalidMapException` with the tile context.
Reusing an entity for multiple tiles is rejected.

All rows are checked before the loader calls the creators. An unreadable
file, malformed UTF-8, unknown character, ragged map, or missing/duplicate
`P` produces `InvalidMapException`. File errors name the path and preserve
their cause. The loader returns a world only after construction succeeds.

M-01 covers text parsing and entity creation. M-02 adds the fixed 14 × 10
dimensions, gameplay counts, walkability, and reachability checks; the
parser currently also accepts smaller rectangular maps for tests. The
diagram's `MapRule` and rule-bearing exceptions are part of M-02. M-05
supplies `GameConfig`, the factory's `config` field, `assignPatrolPaths`,
and the Patrol/Chase split. The real
boss and lava classes belong to E-04/E-05 and M-03; M-01 does not ship
replacement implementations. Its tests use explicit fixtures for those two
types. C-09 connects the loader to `app/GameFactory` and replaces the MVP-0
`StandInMap`.
