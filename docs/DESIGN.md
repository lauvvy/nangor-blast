# DESIGN — Nangor Blast

How the game looks and reads on screen. It covers the terminal view, which is mandatory, and the libGDX window, which is bonus B01. Both draw the same `WorldSnapshot`, so a rule here applies to both unless it names one.

These values are a first proposal by the team lead. Change this file first, then the code.

## Look

Red Mountain is a volcanic mining area: dark rock, with orange, yellow, and red for anything that can hurt the player. Deeper is the only cool color on the map, so the player is always easy to find.

Until `G-01` the GUI draws flat colored tiles with the glyph on top. Sprite art is decided when GUI work starts.

## Glyphs and colors

The first table is the legend from the specification (F07.2) for what exists on Red Mountain. Do not change the glyphs.

| Thing | Glyph | Color | Notes |
| --- | --- | --- | --- |
| Indestructible Block | `#` | `#5C4A42` | |
| Destructible Block | `D` | `#A9713F` | |
| Reinforced Block | `R` | `#7C7F86` | Not on the Red Mountain layout. Used by the Map Maker. |
| Walkable space | `.` | `#3B2A24` | `EntityKind.FLOOR`, implemented by `FloorTile`. Floor terrain is held in `GameMap`; the CLI also draws it beneath occupants. |
| Player | `P` | `#4FC3F7` | Shown dimmed, `#2A6F8E`, while invulnerable. |
| Normal Enemy | `E` | `#B565D8` | |
| Boss | `B` | `#E53935` | Shown brighter while vulnerable. |
| Exit | `X` | `#6D6D6D` locked, `#66BB6A` open | Open once every enemy and the boss are defeated. |
| Lava Tile, DORMANT | `H` | `#8C2F14` | |
| Explosion | `*` | `#FFD54F` | Shown for 0.5 seconds. |
| Warning | `!` | `#FFEB3B` | Boss attack warning, shown for 1 second. |
| Power-Up Bomb Up | `+` | `#26C6DA` | |
| Power-Up Fire Up | `f` | `#26C6DA` | |
| Power-Up Speed Up | `s` | `#26C6DA` | |
| Power-Up Remote Detonator | `r` | `#26C6DA` | |
| Power-Up Heart | `h` | `#26C6DA` | |

The specification has no glyph for these. The team defines them.

| Thing | Glyph | Color | Notes |
| --- | --- | --- | --- |
| Normal Bomb | `o` | `#ECEFF1` | |
| Pumped Bomb | `O` | `#ECEFF1` | |
| Remote Bomb | `@` | `#ECEFF1` | |
| Lava Tile, WARNING | `!` | `#F2A20C` | Reuses the warning glyph on purpose. The tile is about to hurt. |
| Lava Tile, ERUPTING | `^` | `#FF4B1F` | |

Other colors: background `#1A1210`, text `#F5EDE6`, secondary text `#B8A79C`, error and low timer `#FF5252`.

The terminal uses the nearest color it supports. Every state also has its own glyph or brightness, so the game stays readable when a terminal shows few colors.

## Draw priority

Each tile shows one thing. When several are on the same tile, the highest in this list is drawn:

1. Explosion
2. Warning
3. Player
4. Boss
5. Normal Enemy
6. Bomb
7. Power-up
8. Block
9. Exit
10. Lava Tile
11. Floor

## Screen layout

- **HUD** — one line above the map. From left to right: lives, selected bomb type, score, timer. The timer turns to the error color below 30 seconds.
- **Map** — 14 columns by 10 rows, below the HUD.
- **Message line** — one line below the map, for a failed action such as placing a bomb at full capacity. It clears after 2 seconds.
- **Menus** — Main Menu, How to Play, Pause, and Game Over are centered panels. The map behind Pause and Game Over stays visible and dimmed.

## Size and scaling

- **Terminal** — each tile is two characters wide, so tiles look square. The game needs at least 48 columns by 13 rows. A smaller terminal shows a message asking to enlarge it. The screen is redrawn when the terminal is resized.
- **GUI** — the game is drawn on a fixed virtual screen. A tile is 48 units, which makes the map 672 × 480, with the HUD above it and the message line below. A fit viewport scales that screen to the window and keeps its proportions, so the whole map is always visible and leftover space becomes bars in the background color. The HUD and menus are laid out with Scene2D tables, never fixed positions.
- **GUI text** — at least 18 pixels for body text and 20 pixels for the HUD at the reference size. Fonts are generated at the needed size from one open-license monospace font file in `assets/`. libGDX's built-in bitmap font is not used, because it blurs when scaled.
- **GUI sprites** — one sprite per row in the glyph tables, 48 × 48 pixels, packed into one texture atlas in `assets/`. Until the sprites exist, `G-01` draws flat colored tiles.

## Menus

- Every screen opens with one choice selected. The selected choice is marked with `>` in the terminal and a highlighted background in the GUI.
- Up and down move the selection, confirm chooses it, and back returns to the previous screen. In the terminal, typing a choice's number chooses it at once.
- An input that matches no choice shows an error on the message line and the menu stays open.
- Controls shown in How to Play are read from the key bindings, so they are never out of date.

## Feedback

- Every boss attack and every eruption shows its warning for the full 1 second before it can hurt.
- Only the affected tiles change. The whole screen never flashes.
- Losing a life, collecting a power-up, and a refused action each show a short text on the message line.

## Rules

- **Do** add a glyph and a GUI color in the same change when a new `EntityKind` is added.
- **Do** keep warm colors for danger and cool colors for the player and pickups.
- **Don't** use a color that is not in this file. Add it here first.
- **Don't** change a glyph from the specification's legend.
- **Don't** put a game rule in drawing code.
