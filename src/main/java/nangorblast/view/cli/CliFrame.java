package nangorblast.view.cli;

import java.util.Arrays;
import java.util.Locale;
import nangorblast.core.EntityKind;
import nangorblast.core.Position;
import nangorblast.view.WorldSnapshot;
import nangorblast.view.WorldSnapshot.Panel;
import nangorblast.view.WorldSnapshot.Sprite;

/**
 * One screen of the terminal view as a grid of cells, composed from a snapshot with the
 * glyphs, colors, draw priority, and layout in DESIGN.md. Pure: no terminal access, so
 * the renderer can diff two frames and tests can read cells directly.
 *
 * <p>Layout: row 0 is the HUD, row 1 is empty, the map starts at row 2 with each tile two
 * characters wide, and the message line is the row below the map. An open menu is a boxed
 * panel centered over the map, which stays visible and dimmed around it.
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
    static final Glyph PLAYER_INVULNERABLE = new Glyph('P', 0x2A6F8E, 3); // dimmed player, DESIGN.md
    private static final Cell BLANK = new Cell(' ', TEXT);

    /** The exhaustive switch makes a new {@link EntityKind} fail to compile until it has a glyph. */
    static Glyph glyph(EntityKind kind) {
        return switch (kind) {
            case FLOOR -> FLOOR;
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
        Panel panel = snapshot.panel();
        if (panel != null) {
            frame.drawPanel(panel, MAP_TOP + snapshot.height() - 1);
            frame.put(MAP_TOP + snapshot.height(), 0, panel.message(), ERROR);
        }
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
            Glyph glyph = sprite.kind() == EntityKind.PLAYER && snapshot.hud().invulnerable()
                    ? PLAYER_INVULNERABLE : glyph(sprite.kind());
            raise(top, sprite.position(), glyph);
        }
        for (Position warning : snapshot.warnings()) {
            raise(top, warning, WARNING);
        }
        boolean dimmed = snapshot.panel() != null;
        for (int y = 0; y < top.length; y++) {
            for (int x = 0; x < top[y].length; x++) {
                int rgb = dimmed ? dim(top[y][x].rgb()) : top[y][x].rgb();
                cells[MAP_TOP + y][2 * x] = new Cell(top[y][x].ch(), rgb);
            }
        }
    }

    /** Halfway from the color to the background, for the map behind a menu. */
    static int dim(int rgb) {
        int r = ((rgb >> 16 & 0xFF) + (BACKGROUND >> 16 & 0xFF)) / 2;
        int g = ((rgb >> 8 & 0xFF) + (BACKGROUND >> 8 & 0xFF)) / 2;
        int b = ((rgb & 0xFF) + (BACKGROUND & 0xFF)) / 2;
        return r << 16 | g << 8 | b;
    }

    /**
     * A box two columns in from each side, centered between row 1 and {@code lastRow}: the
     * title, the text lines, then the items numbered from 1 with {@code >} on the selected one.
     */
    private void drawPanel(Panel panel, int lastRow) {
        int left = 2;
        int width = cols() - 2 * left;
        int height = 3 + panel.lines().size() + panel.items().size();
        int top = Math.max(1, 1 + (lastRow - height + 1) / 2);
        for (int row = top; row < top + height && row <= lastRow; row++) {
            boolean edge = row == top || row == top + height - 1;
            for (int col = left; col < left + width; col++) {
                boolean side = col == left || col == left + width - 1;
                char ch = edge ? (side ? '+' : '-') : (side ? '|' : ' ');
                cells[row][col] = new Cell(ch, SECONDARY_TEXT);
            }
        }
        int row = top + 1;
        putClipped(row++, left + (width - panel.title().length()) / 2, panel.title(), TEXT, lastRow);
        for (String line : panel.lines()) {
            putClipped(row++, left + 2, line, TEXT, lastRow);
        }
        for (int i = 0; i < panel.items().size(); i++) {
            boolean chosen = i == panel.selected();
            String item = (chosen ? "> " : "  ") + (i + 1) + ". " + panel.items().get(i);
            putClipped(row++, left + 2, item, chosen ? TEXT : SECONDARY_TEXT, lastRow);
        }
    }

    private void putClipped(int row, int col, String text, int fg, int lastRow) {
        if (row <= lastRow) {
            put(row, col, text.substring(0, Math.min(text.length(), cols() - 3 - col)), fg);
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
