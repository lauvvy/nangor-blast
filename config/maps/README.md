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
| `.` | Walkable floor; no entity |
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

The composition root supplies player speed, time limit, and three entity
creation functions to keep configuration and game rules out of the parser:

```java
EntityFactory entities = new EntityFactory(playerSpeed, enemyCreator, bossCreator, lavaCreator);
MapLoader loader = new MapLoader(entities, timeLimit);
GameWorld world = loader.load(Path.of("config", "maps", "red-mountain.txt"), random);
```

Each creator is a `Function<Position, ? extends Entity>` and must return a
fresh, active entity at the requested position. The enemy and boss must
have the matching `EntityKind` and implement `Hostile`; lava must have kind
`LAVA_DORMANT`. The enemy creator can select a configured movement strategy
per spawn. Missing creators fail at construction; incorrect results or
creator failures produce `InvalidMapException` with the tile context.
Reusing an entity for multiple tiles is rejected.

All rows are checked before the loader calls the creators. An unreadable
file, malformed UTF-8, unknown character, ragged map, or missing/duplicate
`P` produces `InvalidMapException`. File errors name the path and preserve
their cause. The loader returns a world only after construction succeeds.

M-01 covers text parsing and entity creation. M-02 adds the fixed 14 × 10
dimensions, gameplay counts, walkability, and reachability checks; the
parser currently also accepts smaller rectangular maps for tests. M-05
supplies configuration, patrol paths, and the Patrol/Chase split. The real
boss and lava classes belong to E-04/E-05 and M-03; M-01 does not ship
replacement implementations. Its tests use explicit fixtures for those two
types. C-09 connects the loader to `app/GameFactory` and replaces the MVP-0
`StandInMap`.
