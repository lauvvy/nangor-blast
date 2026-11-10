package nangorblast.view.cli;

import java.util.Arrays;
import java.util.Locale;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.view.WorldSnapshot;
import nangorblast.view.WorldSnapshot.Sprite;

/**
 * One screen of the terminal view as a grid of cells, composed from a snapshot with the
 * glyphs, colors, draw priority, and layout in DESIGN.md. Pure: no terminal access, so
 * the renderer can diff two frames and tests can read cells directly.
 *
 * <p>Layout: row 0 is the HUD, row 1 is empty, the map starts at row 2 with each tile two
 * characters wide, and the message line is the row below the map.
 */
final class CliFrame {

    static final int MIN_COLS = 48;
    static final int MAP_TOP = 2;
    static final int LOW_TIME_SECONDS = 30;

    static final int BACKGROUND = 0x1A1210;
    static final int TEXT = 0xF5EDE6;
    static final int SECONDARY_TEXT = 0xB8A79C;
    static final int ERROR = 0xFF5252;

    /** One terminal character and its foreground color. The background is always {@link #BACKGROUND}. */
    record Cell(char ch, int fg) {
    }

    /** A glyph from DESIGN.md. Lower priority numbers win when several things share a tile. */
    record Glyph(char ch, int rgb, int priority) {
    }

    static final Glyph WARNING = new Glyph('!', 0xFFEB3B, 2);
    static final Glyph FLOOR = new Glyph('.', 0x3B2A24, 11);
    private static final Cell BLANK = new Cell(' ', TEXT);

    /** The exhaustive switch makes a new {@link EntityKind} fail to compile until it has a glyph. */
    static Glyph glyph(EntityKind kind) {
        return switch (kind) {
            case EXPLOSION -> new Glyph('*', 0xFFD54F, 1);
            case PLAYER -> new Glyph('P', 0x4FC3F7, 3);
            case BOSS -> new Glyph('B', 0xE53935, 4);
            case NORMAL_ENEMY -> new Glyph('E', 0xB565D8, 5);
            case BOMB_NORMAL -> new Glyph('o', 0xECEFF1, 6);
            case BOMB_PUMPED -> new Glyph('O', 0xECEFF1, 6);
            case BOMB_REMOTE -> new Glyph('@', 0xECEFF1, 6);
            case POWER_UP_BOMB_UP -> new Glyph('+', 0x26C6DA, 7);
            case POWER_UP_FIRE_UP -> new Glyph('f', 0x26C6DA, 7);
            case POWER_UP_SPEED_UP -> new Glyph('s', 0x26C6DA, 7);
            case POWER_UP_REMOTE_DETONATOR -> new Glyph('r', 0x26C6DA, 7);
            case POWER_UP_HEART -> new Glyph('h', 0x26C6DA, 7);
            case INDESTRUCTIBLE_BLOCK -> new Glyph('#', 0x5C4A42, 8);
            case DESTRUCTIBLE_BLOCK -> new Glyph('D', 0xA9713F, 8);
            case REINFORCED_BLOCK -> new Glyph('R', 0x7C7F86, 8);
            case EXIT_LOCKED -> new Glyph('X', 0x6D6D6D, 9);
            case EXIT_OPEN -> new Glyph('X', 0x66BB6A, 9);
            case LAVA_DORMANT -> new Glyph('H', 0x8C2F14, 10);
            case LAVA_WARNING -> new Glyph('!', 0xF2A20C, 10);
            case LAVA_ERUPTING -> new Glyph('^', 0xFF4B1F, 10);
        };
    }

    private final Cell[][] cells;

    private CliFrame(int rows, int cols) {
        cells = new Cell[rows][cols];
        for (Cell[] row : cells) {
            Arrays.fill(row, BLANK);
        }
    }

    static int rowsFor(WorldSnapshot snapshot) {
        return MAP_TOP + snapshot.height() + 1;
    }

    static int colsFor(WorldSnapshot snapshot) {
        return Math.max(MIN_COLS, snapshot.width() * 2);
    }

    static CliFrame compose(WorldSnapshot snapshot) {
        CliFrame frame = new CliFrame(rowsFor(snapshot), colsFor(snapshot));
        frame.drawHud(snapshot.hud());
        frame.drawMap(snapshot);
        frame.put(MAP_TOP + snapshot.height(), 0, stateMessage(snapshot), SECONDARY_TEXT);
        return frame;
    }

    private void drawHud(WorldSnapshot.Hud hud) {
        String bomb = hud.selectedBomb().name().charAt(0)
                + hud.selectedBomb().name().substring(1).toLowerCase(Locale.ROOT);
        String left = String.format(Locale.ROOT, "Lives %d  Bomb %-6s  Score %d  Time ", hud.lives(), bomb, hud.score());
        put(0, 0, left, TEXT);
        put(0, left.length(), Integer.toString(hud.secondsLeft()),
                hud.secondsLeft() < LOW_TIME_SECONDS ? ERROR : TEXT);
    }

    private void drawMap(WorldSnapshot snapshot) {
        Glyph[][] top = new Glyph[snapshot.height()][snapshot.width()];
        for (Glyph[] row : top) {
            Arrays.fill(row, FLOOR);
        }
        for (Sprite sprite : snapshot.sprites()) {
            raise(top, sprite.position(), glyph(sprite.kind()));
        }
        for (Position warning : snapshot.warnings()) {
            raise(top, warning, WARNING);
        }
        for (int y = 0; y < top.length; y++) {
            for (int x = 0; x < top[y].length; x++) {
                cells[MAP_TOP + y][2 * x] = new Cell(top[y][x].ch(), top[y][x].rgb());
            }
        }
    }

    private static void raise(Glyph[][] top, Position at, Glyph candidate) {
        if (at.row() < 0 || at.row() >= top.length || at.col() < 0 || at.col() >= top[0].length) {
            return; // never trust a position from outside the renderer to be on the map
        }
        if (candidate.priority() < top[at.row()][at.col()].priority()) {
            top[at.row()][at.col()] = candidate;
        }
    }

    private static String stateMessage(WorldSnapshot snapshot) {
        return switch (snapshot.state()) {
            case PAUSED -> "Paused";
            case WIN -> "You win";
            case LOSE -> "Game over";
            case MENU, PLAYING -> "";
        };
    }

    private void put(int row, int col, String text, int fg) {
        for (int i = 0; i < text.length() && col + i < cells[row].length; i++) {
            cells[row][col + i] = new Cell(text.charAt(i), fg);
        }
    }

    int rows() {
        return cells.length;
    }

    int cols() {
        return cells[0].length;
    }

    Cell cell(int row, int col) {
        return cells[row][col];
    }

    /** The characters of one row, for tests and logs. */
    String rowText(int row) {
        StringBuilder text = new StringBuilder(cols());
        for (Cell cell : cells[row]) {
            text.append(cell.ch());
        }
        return text.toString();
    }
}
